package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.data.api.CreateCustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.ProbationStatus;
import uk.gov.justice.digital.delius.data.api.ProbationStatusDetail;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;
import uk.gov.justice.digital.delius.entitybuilders.EventEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.KeyDateEntityBuilder;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;
import uk.gov.justice.digital.delius.transformers.CustodyKeyDateTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.service.CustodyKeyDatesMapper.custodyManagedKeyDates;
import static uk.gov.justice.digital.delius.service.CustodyKeyDatesMapper.descriptionOf;
import static uk.gov.justice.digital.delius.service.CustodyKeyDatesMapper.keyDatesOf;
import static uk.gov.justice.digital.delius.service.CustodyKeyDatesMapper.missingKeyDateTypesCodes;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
@Slf4j
public class ConvictionService {
    public static final int SENTENCE_START_DATE_LENIENT_DAYS = 7;
    private final Boolean updateCustodyKeyDatesFeatureSwitch;
    private final EventRepository eventRepository;
    private final OffenderRepository offenderRepository;
    private final EventEntityBuilder eventEntityBuilder;
    private final KeyDateEntityBuilder keyDateEntityBuilder;
    private final IAPSNotificationService iapsNotificationService;
    private final SpgNotificationService spgNotificationService;
    private final LookupSupplier lookupSupplier;
    private final ContactService contactService;
    private final TelemetryClient telemetryClient;
    private final FeatureSwitches featureSwitches;

    public static class SingleActiveCustodyConvictionNotFoundException extends BadRequestException {
        public SingleActiveCustodyConvictionNotFoundException(Long offenderId, int activeCustodyConvictionCount) {
            super(String.format("Expected offender %d to have a single custody related event but found %d events", offenderId, activeCustodyConvictionCount));
        }
    }

    public static class DuplicateActiveCustodialConvictionsException extends Exception {
        private final int convictionCount;

        public DuplicateActiveCustodialConvictionsException(int convictionCount) {
            super(String.format("duplicate active custody conviction count was %d, should be 1", convictionCount));
            this.convictionCount = convictionCount;
        }

        public int getConvictionCount() {
            return convictionCount;
        }
    }
    public static class DuplicateConvictionsForSentenceDateException extends Exception {
        private final int convictionCount;

        DuplicateConvictionsForSentenceDateException(int convictionCount) {
            super(String.format("duplicate active custody conviction count was %d, should be 1", convictionCount));
            this.convictionCount = convictionCount;
        }

        public int getConvictionCount() {
            return convictionCount;
        }
    }

    public static class CustodyTypeCodeIsNotValidException extends Exception {
        CustodyTypeCodeIsNotValidException(String message) {
            super(message);
        }
    }
    @Autowired
    public ConvictionService(
        EventRepository eventRepository,
        OffenderRepository offenderRepository,
        EventEntityBuilder eventEntityBuilder,
        SpgNotificationService spgNotificationService,
        LookupSupplier lookupSupplier,
        KeyDateEntityBuilder keyDateEntityBuilder,
        IAPSNotificationService iapsNotificationService,
        ContactService contactService, TelemetryClient telemetryClient, FeatureSwitches featureSwitches) {
        this.updateCustodyKeyDatesFeatureSwitch = featureSwitches.getNoms().getUpdate().isKeyDates();
        this.eventRepository = eventRepository;
        this.offenderRepository = offenderRepository;
        this.eventEntityBuilder = eventEntityBuilder;
        this.keyDateEntityBuilder = keyDateEntityBuilder;
        this.spgNotificationService = spgNotificationService;
        this.lookupSupplier = lookupSupplier;
        this.iapsNotificationService = iapsNotificationService;
        this.contactService = contactService;
        this.telemetryClient = telemetryClient;
        this.featureSwitches = featureSwitches;
    }

    @Transactional(readOnly = true)
    public List<Conviction> convictionsFor(Long offenderId) {
        List<uk.gov.justice.digital.delius.jpa.standard.entity.Event> events = eventRepository.findByOffenderId(offenderId);
        return events
                .stream()
                .filter(event -> !convertToBoolean(event.getSoftDeleted()))
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Event::getReferralDate).reversed())
                .map(ConvictionTransformer::convictionOf)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public Optional<Conviction> convictionFor(Long offenderId, Long eventId) {
        val event = eventRepository.findById(eventId);
        return event
            .filter(e ->  offenderId.equals(e.getOffenderId()))
            .filter(e -> !convertToBoolean(e.getSoftDeleted()))
            .map(ConvictionTransformer::convictionOf);
    }

    @Transactional
    public Conviction addCourtCaseFor(Long offenderId, CourtCase courtCase) {
        val event = eventEntityBuilder.eventOf(
                offenderId,
                courtCase,
                calculateNextEventNumber(offenderId));

        val conviction = ConvictionTransformer.convictionOf(eventRepository.save(event));
        spgNotificationService.notifyNewCourtCaseCreated(event);
        return conviction;
    }

    @Transactional(readOnly = true)
    public Optional<Long> getConvictionIdByPrisonBookingNumber(String prisonBookingNumber) throws DuplicateActiveCustodialConvictionsException {
        val events = eventRepository.findByPrisonBookingNumber(prisonBookingNumber);

        if (events.size() == 1) {
            return firstEventId(events);
        }

        // allow being relaxed and allow inactive events to be filtered out
        val activeEvents = activeEvents(events);

        switch (activeEvents.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return firstEventId(activeEvents);
            default:
                throw new DuplicateActiveCustodialConvictionsException(activeEvents.size());
        }
    }

    public Optional<Event> getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(Long offenderId, String prisonBookingNumber) throws DuplicateActiveCustodialConvictionsException {
        val events = activeCustodyEvents(offenderId);

        switch (events.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return events.stream().filter(event -> prisonBookingNumber.equals(event.getDisposal().getCustody().getPrisonerNumber())).findFirst();
            default:
                throw new DuplicateActiveCustodialConvictionsException(events.size());
        }
    }

    @Transactional(readOnly = true)
    public Optional<Long> getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(Long offenderId, String prisonBookingNumber) throws DuplicateActiveCustodialConvictionsException {
        return getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(offenderId, prisonBookingNumber).map(Event::getEventId);
    }

    public Result<Optional<Event>, DuplicateConvictionsForSentenceDateException> getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(Long offenderId, LocalDate sentenceStartDate) {
        val events = activeCustodyEvents(offenderId)
                .stream()
                .filter(event -> didSentenceStartAroundDate(event, sentenceStartDate))
                .collect(toList());

        switch (events.size()) {
            case 0:
                return Result.of(Optional.empty());
            case 1:
                return Result.of(events.stream().findFirst());
            default:
                return Result.ofError(new DuplicateConvictionsForSentenceDateException(events.size()));
        }
    }

    private boolean didSentenceStartAroundDate(Event event, LocalDate sentenceStartDate) {
        // typically used to match start dates in NOMIS and Delius which may be out by a few days
        return Math.abs(DAYS.between(event.getDisposal().getStartDate(), sentenceStartDate)) <= SENTENCE_START_DATE_LENIENT_DAYS;
    }


    @Transactional
    public CustodyKeyDate addOrReplaceCustodyKeyDateByOffenderId(Long offenderId, String typeCode, CreateCustodyKeyDate custodyKeyDate) throws CustodyTypeCodeIsNotValidException {
        var custodialEvents = getAllActiveCustodialEvents(offenderId);
        if (custodialEvents.isEmpty()) {
            throw new SingleActiveCustodyConvictionNotFoundException(offenderId, 0);
        }

        // legacy behaviour - do not update multiple events
        if (!featureSwitches.getNoms().getUpdate().getMultipleEvents().isUpdateKeyDates() && custodialEvents.size() > 1) {
            throw new SingleActiveCustodyConvictionNotFoundException(offenderId, custodialEvents.size());
        }

        val custodyKeyDateType = lookupSupplier.custodyKeyDateTypeSupplier().apply(typeCode)
            .orElseThrow(() -> new CustodyTypeCodeIsNotValidException(String.format("%s is not a valid custody key date", typeCode)));

        return custodialEvents
            .stream()
            .map(event -> addOrReplaceCustodyKeyDate(event, custodyKeyDateType, custodyKeyDate, true))
            .reduce(this::any)
            .orElseThrow();
    }

    private CustodyKeyDate any(CustodyKeyDate one, CustodyKeyDate two) {
        return one;
    }

    @Transactional
    public CustodyKeyDate addOrReplaceCustodyKeyDateByConvictionId(Long convictionId, String typeCode, CreateCustodyKeyDate custodyKeyDate) throws CustodyTypeCodeIsNotValidException {
        return addOrReplaceCustodyKeyDate(eventRepository.getOne(convictionId), typeCode, custodyKeyDate, true);
    }

    @Transactional(readOnly = true)
    public Optional<CustodyKeyDate> getCustodyKeyDateByOffenderId(Long offenderId, String typeCode) {
        return getCustodyKeyDate(getActiveCustodialEvent(offenderId), typeCode);
    }

    @Transactional(readOnly = true)
    public Optional<CustodyKeyDate> getCustodyKeyDateByConvictionId(Long convictionId, String typeCode) {
        return getCustodyKeyDate(eventRepository.getOne(convictionId), typeCode);
    }

    @Transactional(readOnly = true)
    public List<CustodyKeyDate> getCustodyKeyDatesByOffenderId(Long offenderId) {
        return getCustodyKeyDates(getActiveCustodialEvent(offenderId));
    }

    @Transactional(readOnly = true)
    public List<CustodyKeyDate> getCustodyKeyDatesByConvictionId(Long convictionId) {
        return getCustodyKeyDates(eventRepository.getOne(convictionId));
    }

    @Transactional
    public void deleteCustodyKeyDateByOffenderId(Long offenderId, String typeCode) {
        var custodialEvents = getAllActiveCustodialEvents(offenderId);
        if (custodialEvents.isEmpty()) {
            throw new SingleActiveCustodyConvictionNotFoundException(offenderId, 0);
        }

        // legacy behaviour - do not update multiple events
        if (!featureSwitches.getNoms().getUpdate().getMultipleEvents().isUpdateKeyDates() && custodialEvents.size() > 1) {
            throw new SingleActiveCustodyConvictionNotFoundException(offenderId, custodialEvents.size());
        }

        custodialEvents.forEach(event -> deleteCustodyKeyDate(event, typeCode, true));
    }

    @Transactional
    public void deleteCustodyKeyDateByConvictionId(Long convictionId, String typeCode) {
        deleteCustodyKeyDate(eventRepository.getOne(convictionId), typeCode, true);
    }

    @Transactional(readOnly = true)
    public Event getActiveCustodialEvent(Long offenderId) {
        val activeCustodyConvictions = activeCustodyEvents(offenderId);

        if (activeCustodyConvictions.size() != 1) {
            throw new SingleActiveCustodyConvictionNotFoundException(offenderId, activeCustodyConvictions.size());
        }
        return activeCustodyConvictions.get(0);
    }

    @Transactional(readOnly = true)
    public List<Event> getAllActiveCustodialEvents(Long offenderId) {
        return activeCustodyEvents(offenderId);
    }

    @Transactional(readOnly = true)
    public List<Event> getAllActiveCustodialEventsWithBookingNumber(Long offenderId, String bookingNumber) {
        return activeCustodyEvents(offenderId)
            .stream()
            .filter(event -> bookingNumber.equals(event.getDisposal().getCustody().getPrisonerNumber()))
            .collect(toList());
    }

    @Transactional
    public Custody addOrReplaceOrDeleteCustodyKeyDates(Long offenderId, Long convictionId, ReplaceCustodyKeyDates replaceCustodyKeyDates) {
        var event = eventRepository.findById(convictionId).orElseThrow();

        if (updateCustodyKeyDatesFeatureSwitch) {
            final var custodyManagedKeyDates = custodyManagedKeyDates();
            final var missingKeyDateTypesCodes = missingKeyDateTypesCodes(replaceCustodyKeyDates);
            final var currentKeyDates = event
                    .getDisposal()
                    .getCustody()
                    .getKeyDates();

            final var keyDatesToDelete = keyDatesToDelete(custodyManagedKeyDates, missingKeyDateTypesCodes, currentKeyDates);
            final var keyDatesToBeAddedOrUpdated = keyDatesToBeAddedOrUpdated(replaceCustodyKeyDates, currentKeyDates);

            addContactForBulkCustodyKeyDateUpdate(offenderId, event, currentKeyDates, keyDatesToDelete, keyDatesToBeAddedOrUpdated);
            addBulkTelemetry(offenderId, event, currentKeyDates, keyDatesToDelete, keyDatesToBeAddedOrUpdated);

            keyDatesToDelete
                    .forEach(keyDate -> deleteCustodyKeyDate(event, keyDate, false));

            keyDatesToBeAddedOrUpdated
                    .forEach((key, value) -> addOrReplaceCustodyKeyDate(event, key, value));

        } else {
            log.warn("Update custody key dates will be ignored, this feature is switched off ");
        }

        return ConvictionTransformer.custodyOf(event
                .getDisposal()
                .getCustody());
    }

    public Optional<ProbationStatusDetail> probationStatusFor(String crn) {
        return offenderRepository.findByCrn(crn)
            .filter(offender -> offender.getSoftDeleted() == 0L)
            .map((offender) -> new ProbationStatusDetail(
                probationStatusOf(offender),
                previouslyKnownTerminationDateOf(offender),
                inBreachOf(offender),
                preSentenceActivityOf(offender)));
    }

    private ProbationStatus probationStatusOf(Offender offender) {
        if (offender.getCurrentDisposal() == 1)
            return ProbationStatus.CURRENT;
        if (offender.getEvents().stream().anyMatch(event -> event.getDisposal() != null))
            return ProbationStatus.PREVIOUSLY_KNOWN;
        return ProbationStatus.NOT_SENTENCED;
    }

    private LocalDate previouslyKnownTerminationDateOf(Offender offender) {
        if (!probationStatusOf(offender).equals(ProbationStatus.PREVIOUSLY_KNOWN))
            return null;
        return Optional.ofNullable(offender.getEvents())
            .orElse(Collections.emptyList())
            .stream()
            .filter(conviction -> conviction.getDisposal() != null && conviction.getDisposal().getTerminationDate() != null)
            .map(conviction -> conviction.getDisposal().getTerminationDate())
            .max(Comparator.comparing(LocalDate::toEpochDay))
            .orElse(null);
    }

    private Boolean inBreachOf(Offender offender) {
        if (!probationStatusOf(offender).equals(ProbationStatus.CURRENT))
            return null;
        return Optional.of(offender)
            .map(o ->
                activeEvents(o.getEvents())
                    .stream()
                    .anyMatch(event -> event.getInBreach() == 1L)
            ).orElse(null);
    }

    private Boolean preSentenceActivityOf(Offender offender) {
        return Optional.of(offender)
            .map((o) -> o.getEvents()
                .stream()
                .anyMatch(event -> event.getDisposal() == null)
            ).orElse(false);
    }

    private void addBulkTelemetry(Long offenderId, Event event, List<KeyDate> currentKeyDates, List<String> keyDatesToDelete, Map<String, LocalDate> keyDatesToBeAddedOrUpdated) {
        final var offender = offenderRepository.findByOffenderId(offenderId).orElseThrow();
        final var currentManagedDates = currentManagedKeyDates(custodyManagedKeyDates(), currentKeyDates);
        final var datesAmendedOrUpdated = datesAddedOrUpdated(keyDatesToBeAddedOrUpdated);
        final var keyDatesToBeAdded = keyDatesToBeAdded(keyDatesToBeAddedOrUpdated, currentKeyDates);
        final var datesRemoved = datesRemoved(currentKeyDates, keyDatesToDelete);
        final var numberOfDatesUpdated = datesAmendedOrUpdated.size() - keyDatesToBeAdded.size();
        final var attributes = Map.of(
            "crn", offender.getCrn(),
            "offenderNo", Optional.ofNullable(offender.getNomsNumber()).orElse(""),
            "eventId", String.valueOf(event.getEventId()),
            "eventNumber", event.getEventNumber(),
            "updated", String.valueOf(numberOfDatesUpdated),
            "deleted", String.valueOf(datesRemoved.size()),
            "inserted", String.valueOf(keyDatesToBeAdded.size()),
            "unchanged", String.valueOf(currentManagedDates.size() - datesRemoved.size() - numberOfDatesUpdated)
        );

        if (keyDatesToDelete.isEmpty() && keyDatesToBeAddedOrUpdated.isEmpty()) {
            telemetryClient.trackEvent("keyDatesBulkUnchanged",
                attributes,
                null);
        } else {
            telemetryClient.trackEvent("keyDatesBulkSummary",
                attributes,
                null);

            if (!datesRemoved.isEmpty()) {
                final Map<String, String> attributesWithDates = Stream
                    .concat(datesRemoved.entrySet().stream(), attributes.entrySet().stream())
                    .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> e.getValue().toString()
                    ));
                if (currentManagedDates.size() == datesRemoved.size()) {
                    telemetryClient.trackEvent("keyDatesBulkAllRemoved",
                        attributesWithDates,
                        null);
                } else {
                    telemetryClient.trackEvent("keyDatesBulkSomeRemoved",
                        attributesWithDates,
                        null);
                }
            }
        }
    }

    private void addContactForBulkCustodyKeyDateUpdate(Long offenderId, Event event, List<KeyDate> currentKeyDates, List<String> keyDatesToDelete, Map<String, LocalDate> keyDatesToBeAddedOrUpdated) {
        final var datesAmendedOrUpdated = datesAddedOrUpdated(keyDatesToBeAddedOrUpdated);
        final var datesRemoved = datesRemoved(currentKeyDates, keyDatesToDelete);


        if (!keyDatesToDelete.isEmpty() || !keyDatesToBeAddedOrUpdated.isEmpty()) {
            contactService.addContactForBulkCustodyKeyDateUpdate(offenderRepository.findByOffenderId(offenderId)
                    .orElseThrow(), event, datesAmendedOrUpdated, datesRemoved);
        }
    }

    private Map<String, LocalDate> datesAddedOrUpdated(Map<String, LocalDate> keyDatesToBeAddedOrUpdated) {
        return keyDatesToBeAddedOrUpdated.entrySet().stream()
                .collect(Collectors.toMap(entry -> descriptionOf(entry.getKey()), Map.Entry::getValue));
    }

    private Map<String, LocalDate> datesRemoved(List<KeyDate> currentKeyDates, List<String> keyDatesToDelete) {
        return keyDatesToDelete.stream().map(keyDateCode -> currentKeyDates.stream()
                .filter(keyDate -> keyDate.getKeyDateType().getCodeValue().equals(keyDateCode)).findAny()
                .orElseThrow())
                .collect(Collectors.toMap(keyDate -> descriptionOf(keyDate.getKeyDateType()
                        .getCodeValue()), KeyDate::getKeyDate));
    }

    private Map<String, LocalDate> keyDatesToBeAddedOrUpdated(ReplaceCustodyKeyDates replaceCustodyKeyDates, List<KeyDate> currentKeyDates) {
        return keyDatesOf(replaceCustodyKeyDates).entrySet().stream()
                .filter(entry -> hasChangedOrIsNew(currentKeyDates, entry))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<String> keyDatesToBeAdded(Map<String, LocalDate> keyDatesToBeAddedOrUpdated, List<KeyDate> currentKeyDates) {
        return keyDatesToBeAddedOrUpdated.keySet().stream()
                .filter(keyDateCode -> isNew(currentKeyDates, keyDateCode))
                .collect(toList());
    }

    private List<String> keyDatesToDelete(List<String> custodyManagedKeyDates, List<String> missingKeyDateTypesCodes, List<KeyDate> currentKeyDates) {
        return currentKeyDates
                .stream()
                .map(KeyDate::getKeyDateType)
                .map(StandardReference::getCodeValue)
                .filter(custodyManagedKeyDates::contains)  // all key dates managed by this service
                .filter(missingKeyDateTypesCodes::contains) // all ones missing from request
                .collect(toList());
    }

    private List<String> currentManagedKeyDates(List<String> custodyManagedKeyDates, List<KeyDate> currentKeyDates) {
        return currentKeyDates
                .stream()
                .map(KeyDate::getKeyDateType)
                .map(StandardReference::getCodeValue)
                .filter(custodyManagedKeyDates::contains)  // all key dates managed by this service
                .collect(toList());
    }

    private boolean hasChangedOrIsNew(List<KeyDate> currentKeyDates, Map.Entry<String, LocalDate> entry) {
        // if can't find item with matching code and date then must be new or changed
        return currentKeyDates.stream()
                .filter(keyDate -> keyDate.getKeyDateType().getCodeValue().equals(entry.getKey()))
                .filter(keyDate -> keyDate.getKeyDate().equals(entry.getValue())).findAny().isEmpty();
    }

    private boolean isNew(List<KeyDate> currentKeyDates, String keyDateCode) {
        // if can't find item with matching code then must be new
        return currentKeyDates.stream()
                .filter(keyDate -> keyDate.getKeyDateType().getCodeValue().equals(keyDateCode))
                .findAny().isEmpty();
    }

    private String calculateNextEventNumber(Long offenderId) {
        return String.valueOf(eventRepository.findByOffenderId(offenderId).size() + 1);
    }

    private List<Event> activeEvents(List<Event> events) {
        return events.stream()
            .filter(event -> event.getSoftDeleted() == 0L)
            .filter(event -> event.getActiveFlag() == 1L)
            .collect(toList());
    }

    private List<Event> activeCustodyEvents(Long offenderId) {
        return eventRepository.findActiveByOffenderIdWithCustody(offenderId)
                .stream()
                .filter(event -> event.getDisposal().getTerminationDate() == null)
                .filter(event -> !event.getDisposal().getCustody().isPostSentenceSupervision())
                .collect(toList());
    }

    private Optional<CustodyKeyDate> getCustodyKeyDate(Event event, String typeCode) {
        return event
                .getDisposal()
                .getCustody()
                .getKeyDates()
                .stream()
                .filter(matchTypeCode(typeCode))
                .findAny()
                .map(CustodyKeyDateTransformer::custodyKeyDateOf);
    }

    private Predicate<KeyDate> matchTypeCode(String typeCode) {
        return keyDate -> keyDate.getKeyDateType().getCodeValue().equals(typeCode);
    }

    private void addOrReplaceCustodyKeyDate(Event event, String typeCode, LocalDate date)  {
        try {
            addOrReplaceCustodyKeyDate(event, typeCode, CreateCustodyKeyDate.builder().date(date).build(), false);
        } catch (CustodyTypeCodeIsNotValidException e) {
            throw new RuntimeException(e);
        }
    }
    private CustodyKeyDate addOrReplaceCustodyKeyDate(Event event, String typeCode, CreateCustodyKeyDate custodyKeyDate, boolean shouldNotifyIAPS) throws CustodyTypeCodeIsNotValidException {
        val custodyKeyDateType = lookupSupplier.custodyKeyDateTypeSupplier().apply(typeCode)
                .orElseThrow(() -> new CustodyTypeCodeIsNotValidException(String.format("%s is not a valid custody key date", typeCode)));

        return addOrReplaceCustodyKeyDate(event, custodyKeyDateType, custodyKeyDate, shouldNotifyIAPS);
    }
    private CustodyKeyDate addOrReplaceCustodyKeyDate(Event event, StandardReference custodyKeyDateType, CreateCustodyKeyDate custodyKeyDate, boolean shouldNotifyIAPS) {
        final var typeCode = custodyKeyDateType.getCodeValue();
        final var telemetryProperties = Map.of("offenderId", event.getOffenderId().toString(),
                "eventId", event.getEventId().toString(),
                "eventNumber", event.getEventNumber(),
                "type", typeCode,
                "date", custodyKeyDate.getDate().toString());


        val maybeExistingKeyDate = event.getDisposal().getCustody().getKeyDates()
                .stream()
                .filter(matchTypeCode(typeCode))
                .findAny();

        maybeExistingKeyDate.ifPresent(existingKeyDate -> {
            existingKeyDate.setKeyDate(custodyKeyDate.getDate());
            existingKeyDate.setLastUpdatedDatetime(LocalDateTime.now());
            existingKeyDate.setLastUpdatedUserId(lookupSupplier.userSupplier().get().getUserId());
            eventRepository.save(event);
            spgNotificationService.notifyUpdateOfCustodyKeyDate(typeCode, event);
            telemetryClient.trackEvent("KeyDateUpdated", telemetryProperties, null);
        });

        if (maybeExistingKeyDate.isEmpty()) {
            val keyDate = keyDateEntityBuilder.keyDateOf(event.getDisposal().getCustody(), custodyKeyDateType, custodyKeyDate.getDate());
            event.getDisposal()
                    .getCustody()
                    .getKeyDates()
                    .add(keyDate);

            eventRepository.saveAndFlush(event);
            spgNotificationService.notifyNewCustodyKeyDate(typeCode, event);
            telemetryClient.trackEvent("KeyDateAdded", telemetryProperties, null);
        }

        // Delius does not notify IAPS when the update comes from NOMIS only when done by probation - no idea why so this behaviour but it must be replicated given we have no user needs defined
        if (shouldNotifyIAPS && KeyDate.isSentenceExpiryKeyDate(typeCode)) {
            iapsNotificationService.notifyEventUpdated(event);
        }

        return getCustodyKeyDate(event, typeCode).orElseThrow(() -> new RuntimeException("Added/Updated keyDate has disappeared"));
    }

    private List<CustodyKeyDate> getCustodyKeyDates(Event event) {
        return event.getDisposal().getCustody().getKeyDates()
                .stream()
                .map(CustodyKeyDateTransformer::custodyKeyDateOf)
                .collect(toList());
    }

    private void deleteCustodyKeyDate(Event event, String typeCode, boolean shouldNotifyIAPS) {
        final var telemetryProperties = Map.of("offenderId", event.getOffenderId().toString(),
                "eventId", event.getEventId().toString(),
                "eventNumber", event.getEventNumber(),
                "type", typeCode);

        val keyDates = event.getDisposal().getCustody().getKeyDates();
        val maybeKeyDateToRemove =  keyDates
               .stream()
               .filter(matchTypeCode(typeCode))
               .findAny();

       maybeKeyDateToRemove.ifPresent(keyDateToRemove -> {
           keyDates.remove(keyDateToRemove);
           eventRepository.save(event);
           spgNotificationService.notifyDeletedCustodyKeyDate(keyDateToRemove, event);
           if (shouldNotifyIAPS && KeyDate.isSentenceExpiryKeyDate(typeCode)) {
               iapsNotificationService.notifyEventUpdated(event);
           }
           telemetryClient.trackEvent("KeyDateDeleted", telemetryProperties, null);
       });
    }

    private Optional<Long> firstEventId(List<Event> events) {
        return events.stream().findFirst().map(Event::getEventId);
    }
}

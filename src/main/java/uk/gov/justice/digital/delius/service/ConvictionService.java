package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.data.api.CreateCustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;
import uk.gov.justice.digital.delius.transformers.CustodyKeyDateTransformer;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
public class ConvictionService {
    private final EventRepository eventRepository;
    private final ConvictionTransformer convictionTransformer;
    private final CustodyKeyDateTransformer custodyKeyDateTransformer;
    private final IAPSNotificationService iapsNotificationService;
    private final SpgNotificationService spgNotificationService;
    private final LookupSupplier lookupSupplier;

    public static class SingleActiveCustodyConvictionNotFoundException extends RuntimeException {
        public SingleActiveCustodyConvictionNotFoundException(Long offenderId, int activeCustodyConvictionCount) {
            super(String.format("Expected offender %d to have a single custody related event but found %d events", offenderId, activeCustodyConvictionCount));
        }
    }

    public static class DuplicateConvictionsForBookingNumberException extends Exception {
        private final int convictionCount;

        DuplicateConvictionsForBookingNumberException(int convictionCount) {
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
            ConvictionTransformer convictionTransformer,
            SpgNotificationService spgNotificationService,
            LookupSupplier lookupSupplier,
            CustodyKeyDateTransformer custodyKeyDateTransformer,
            IAPSNotificationService iapsNotificationService) {
        this.eventRepository = eventRepository;
        this.convictionTransformer = convictionTransformer;
        this.spgNotificationService = spgNotificationService;
        this.lookupSupplier = lookupSupplier;
        this.custodyKeyDateTransformer = custodyKeyDateTransformer;
        this.iapsNotificationService = iapsNotificationService;
    }

    @Transactional(readOnly = true)
    public List<Conviction> convictionsFor(Long offenderId) {
        List<uk.gov.justice.digital.delius.jpa.standard.entity.Event> events = eventRepository.findByOffenderId(offenderId);
        return events
                .stream()
                .filter(event -> !convertToBoolean(event.getSoftDeleted()))
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Event::getReferralDate).reversed())
                .map(convictionTransformer::convictionOf)
                .collect(toList());
    }

    @Transactional
    public Conviction addCourtCaseFor(Long offenderId, CourtCase courtCase) {
        val event = convictionTransformer.eventOf(
                offenderId,
                courtCase,
                calculateNextEventNumber(offenderId));

        val conviction = convictionTransformer.convictionOf(eventRepository.save(event));
        spgNotificationService.notifyNewCourtCaseCreated(event);
        return conviction;
    }

    @Transactional(readOnly = true)
    public Optional<Long> getConvictionIdByPrisonBookingNumber(String prisonBookingNumber) throws DuplicateConvictionsForBookingNumberException {
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
                throw new DuplicateConvictionsForBookingNumberException(activeEvents.size());
        }
    }

    @Transactional
    public CustodyKeyDate addOrReplaceCustodyKeyDateByOffenderId(Long offenderId, String typeCode, CreateCustodyKeyDate custodyKeyDate) throws CustodyTypeCodeIsNotValidException {
        return addOrReplaceCustodyKeyDate(getActiveCustodialEvent(offenderId), typeCode, custodyKeyDate);
    }

    @Transactional
    public CustodyKeyDate addOrReplaceCustodyKeyDateByConvictionId(Long convictionId, String typeCode, CreateCustodyKeyDate custodyKeyDate) throws CustodyTypeCodeIsNotValidException {
        return addOrReplaceCustodyKeyDate(eventRepository.getOne(convictionId), typeCode, custodyKeyDate);
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
        deleteCustodyKeyDate(getActiveCustodialEvent(offenderId), typeCode);
    }

    @Transactional
    public void deleteCustodyKeyDateByConvictionId(Long convictionId, String typeCode) {
        deleteCustodyKeyDate(eventRepository.getOne(convictionId), typeCode);
    }

    @Transactional(readOnly = true)
    public Event getActiveCustodialEvent(Long offenderId) {
        val activeCustodyConvictions = activeCustodyEvents(offenderId);

        if (activeCustodyConvictions.size() != 1) {
            throw new SingleActiveCustodyConvictionNotFoundException(offenderId, activeCustodyConvictions.size());
        }
        return activeCustodyConvictions.get(0);
    }

    private String calculateNextEventNumber(Long offenderId) {
        return String.valueOf(eventRepository.findByOffenderId(offenderId).size() + 1);
    }

    private List<Event> activeEvents(List<Event> events) {
        return events.stream().filter(event -> event.getActiveFlag() == 1L).collect(toList());
    }

    private List<Event> activeCustodyEvents(Long offenderId) {
        return eventRepository
                .findByOffenderId(offenderId)
                .stream()
                .filter(event -> event.getSoftDeleted() == 0L)
                .filter(event -> event.getActiveFlag() == 1L)
                .filter(event -> event.getDisposal() != null)
                .filter(event -> event.getDisposal().getTerminationDate() == null)
                .filter(event -> event.getDisposal().getDisposalType() != null)
                .filter(event -> event.getDisposal().getDisposalType().isCustodial())
                .filter(event -> event.getDisposal().getCustody() != null)
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
                .map(custodyKeyDateTransformer::custodyKeyDateOf);
    }

    private Predicate<KeyDate> matchTypeCode(String typeCode) {
        return keyDate -> keyDate.getKeyDateType().getCodeValue().equals(typeCode);
    }

    private CustodyKeyDate addOrReplaceCustodyKeyDate(Event event, String typeCode, CreateCustodyKeyDate custodyKeyDate) throws CustodyTypeCodeIsNotValidException {
        val custodyKeyDateType = lookupSupplier.custodyKeyDateTypeSupplier().apply(typeCode)
                .orElseThrow(() -> new CustodyTypeCodeIsNotValidException(String.format("%s is not a valid custody key date", typeCode)));


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
        });

        if (!maybeExistingKeyDate.isPresent()) {
            val keyDate = custodyKeyDateTransformer.keyDateOf(event.getDisposal().getCustody(), custodyKeyDateType, custodyKeyDate.getDate());
            event.getDisposal()
                    .getCustody()
                    .getKeyDates()
                    .add(keyDate);

            eventRepository.saveAndFlush(event);
            spgNotificationService.notifyNewCustodyKeyDate(typeCode, event);
        }

        if (KeyDate.isSentenceExpiryKeyDate(typeCode)) {
            iapsNotificationService.notifyEventUpdated(event);
        }

        return getCustodyKeyDate(event, typeCode).orElseThrow(() -> new RuntimeException("Added/Updated keyDate has disappeared"));
    }

    private List<CustodyKeyDate> getCustodyKeyDates(Event event) {
        return event.getDisposal().getCustody().getKeyDates()
                .stream()
                .map(custodyKeyDateTransformer::custodyKeyDateOf)
                .collect(toList());
    }

    private void deleteCustodyKeyDate(Event event, String typeCode) {
        val keyDates = event.getDisposal().getCustody().getKeyDates();
        val maybeKeyDateToRemove =  keyDates
               .stream()
               .filter(matchTypeCode(typeCode))
               .findAny();

       maybeKeyDateToRemove.ifPresent(keyDateToRemove -> {
           keyDates.remove(keyDateToRemove);
           eventRepository.save(event);
           spgNotificationService.notifyDeletedCustodyKeyDate(keyDateToRemove, event);
           if (KeyDate.isSentenceExpiryKeyDate(typeCode)) {
               iapsNotificationService.notifyEventUpdated(event);
           }
       });
    }

    private Optional<Long> firstEventId(List<Event> events) {
        return events.stream().findFirst().map(Event::getEventId);
    }
}

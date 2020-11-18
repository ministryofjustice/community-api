package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import io.vavr.control.Either;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;
import uk.gov.justice.digital.delius.data.api.UpdateCustodyBookingNumber;
import uk.gov.justice.digital.delius.jpa.standard.entity.CustodyHistory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.repository.CustodyHistoryRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@Slf4j
public class CustodyService {
    private final Boolean updateCustodyFeatureSwitch;
    private final Boolean updateBookingNumberFeatureSwitch;
    private final TelemetryClient telemetryClient;
    private final OffenderRepository offenderRepository;
    private final ConvictionService convictionService;
    private final InstitutionRepository institutionRepository;
    private final CustodyHistoryRepository custodyHistoryRepository;
    private final ReferenceDataService referenceDataService;
    private final SpgNotificationService spgNotificationService;
    private final OffenderManagerService offenderManagerService;
    private final ContactService contactService;
    private final OffenderPrisonerService offenderPrisonerService;

    public CustodyService(
            @Value("${features.noms.update.custody}")
                    Boolean updateCustodyFeatureSwitch,
            @Value("${features.noms.update.booking.number}")
                    Boolean updateBookingNumberFeatureSwitch,
            TelemetryClient telemetryClient,
            OffenderRepository offenderRepository,
            ConvictionService convictionService,
            InstitutionRepository institutionRepository,
            CustodyHistoryRepository custodyHistoryRepository,
            ReferenceDataService referenceDataService,
            SpgNotificationService spgNotificationService,
            OffenderManagerService offenderManagerService,
            ContactService contactService, OffenderPrisonerService offenderPrisonerService) {
        this.updateCustodyFeatureSwitch = updateCustodyFeatureSwitch;
        this.updateBookingNumberFeatureSwitch = updateBookingNumberFeatureSwitch;
        this.telemetryClient = telemetryClient;
        this.offenderRepository = offenderRepository;
        this.convictionService = convictionService;
        this.institutionRepository = institutionRepository;
        this.custodyHistoryRepository = custodyHistoryRepository;
        this.referenceDataService = referenceDataService;
        this.spgNotificationService = spgNotificationService;
        this.offenderManagerService = offenderManagerService;
        this.contactService = contactService;
        this.offenderPrisonerService = offenderPrisonerService;
        log.info("NOMIS update custody location feature is {}", updateCustodyFeatureSwitch ? "ON" : "OFF");
        log.info("NOMIS update booking number feature is {}", updateBookingNumberFeatureSwitch ? "ON" : "OFF");
    }

    @Transactional
    public Custody updateCustodyPrisonLocation(final String nomsNumber,
                                               final String bookingNumber,
                                               final UpdateCustody updateCustody) {
        final var telemetryProperties = Map.of("offenderNo", nomsNumber,
                "bookingNumber", bookingNumber,
                "toAgency", updateCustody.getNomsPrisonInstitutionCode());

        final var result = updateCustodyPrisonLocation(nomsNumber,
                offender  -> getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(offender.getOffenderId(), bookingNumber),
                updateCustody.getNomsPrisonInstitutionCode());
        return result.map(success -> {
            switch (success.outcome) {
                case Updated:
                    telemetryClient.trackEvent("P2PTransferPrisonUpdated", telemetryProperties, null);
                    break;
                case NoUpdateRequired:
                    telemetryClient.trackEvent("P2PTransferPrisonUpdateIgnored", telemetryProperties, null);
                    break;
            }
            return success.custody;
        }).getOrElseThrow((error -> {
            switch (error.reason) {
                case TransferPrisonNotFound:
                    telemetryClient.trackEvent("P2PTransferPrisonNotFound", telemetryProperties, null);
                    break;
                case CustodialSentenceNotFoundInCorrectState:
                    telemetryClient.trackEvent("P2PTransferPrisonUpdateIgnored", telemetryProperties, null);
                    break;
                case ConvictionNotFound:
                    telemetryClient.trackEvent("P2PTransferBookingNumberNotFound", telemetryProperties, null);
                    break;
                case MultipleCustodialSentences:
                    telemetryClient.trackEvent("P2PTransferBookingNumberHasDuplicates", telemetryProperties, null);
                    break;
                case OffenderNotFound:
                    telemetryClient.trackEvent("P2PTransferOffenderNotFound", telemetryProperties, null);
                    break;
            }
            return new NotFoundException(error.getMessage());
        }));
    }

    @Transactional
    public void updateCustodyPrisonLocation(final String nomsNumber, final String nomsPrisonInstitutionCode) {
        final var telemetryProperties = Map.of("offenderNo", nomsNumber,
                "toAgency", nomsPrisonInstitutionCode);

        final var result = updateCustodyPrisonLocation(nomsNumber,
                this::getSingleActiveCustodialEvent,
                nomsPrisonInstitutionCode);
        final Optional<String> telemetryName = result.fold(error -> {
            switch (error.reason) {
                case TransferPrisonNotFound:
                    return Optional.of("POMLocationPrisonNotFound");
                case CustodialSentenceNotFoundInCorrectState:
                    return Optional.of("POMLocationCustodialStatusNotCorrect");
                case ConvictionNotFound:
                    return Optional.of("POMLocationNoEvents");
                case MultipleCustodialSentences:
                    return Optional.of("POMLocationMultipleEvents");
                case OffenderNotFound:
                    return Optional.of("POMLocationOffenderNotFound");
            }
            return Optional.empty();
        }, success -> {
            switch (success.outcome) {
                case Updated:
                    return Optional.of("POMLocationUpdated");
                case NoUpdateRequired:
                    return Optional.of("POMLocationCorrect");
            }
            return Optional.empty();
        });

        telemetryName.ifPresent(name -> telemetryClient.trackEvent(name, telemetryProperties, null));

    }


    private Either<PrisonLocationUpdateError, PrisonLocationUpdateSuccess> updateCustodyPrisonLocation(final String nomsNumber,
                                                                                                       final Function<Offender, Either<PrisonLocationUpdateError, Event>> eventSupplier,
                                                                                                       final String nomsPrisonInstitutionCode) {
        return findByNomsNumber(nomsNumber)
                .flatMap(offender -> eventSupplier.apply(offender)
                        .flatMap(event -> isInCustodyOrAboutToStartACustodySentence(event)
                                .flatMap(notUsed -> findByNomisCdeCode(nomsPrisonInstitutionCode)
                                        .flatMap(institution -> updateInstitutionWhenDifferent(offender, event, institution)))
                        ));
    }


    @Transactional
    public Custody updateCustodyBookingNumber(String nomsNumber, UpdateCustodyBookingNumber updateCustodyBookingNumber) {
        final var telemetryProperties = Map.of("offenderNo", nomsNumber,
                "bookingNumber", updateCustodyBookingNumber.getBookingNumber(),
                "sentenceStartDate", updateCustodyBookingNumber.getSentenceStartDate().format(DateTimeFormatter.ISO_DATE));

        final var offender = offenderRepository.findByNomsNumber(nomsNumber).orElseThrow(() -> {
            telemetryClient.trackEvent("P2PImprisonmentStatusOffenderNotFound", telemetryProperties, null);
            return new NotFoundException(String.format("offender with nomsNumber %s not found", nomsNumber));
        });
        final var event = convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(offender.getOffenderId(), updateCustodyBookingNumber.getSentenceStartDate())
                .onError(error -> {
                    telemetryClient.trackEvent("P2PImprisonmentStatusCustodyEventsHasDuplicates", telemetryProperties, null);
                    return new NotFoundException(String.format("no single conviction with sentence date around %s found, instead %d duplicates found", updateCustodyBookingNumber.getSentenceStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE), error.getConvictionCount()));
                }).orElseThrow(() -> {
                    telemetryClient.trackEvent("P2PImprisonmentStatusCustodyEventNotFound", telemetryProperties, null);
                    return new NotFoundException(String.format("conviction with sentence date close to  %s not found", updateCustodyBookingNumber.getSentenceStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
                });

        final var maybeExistingBookingNumber = Optional.ofNullable(event.getDisposal().getCustody().getPrisonerNumber()).filter(StringUtils::isNotBlank);
        final Predicate<String> sameAsNewBookingNumber = existingBookingNumber -> existingBookingNumber.equals(updateCustodyBookingNumber.getBookingNumber());

        if (maybeExistingBookingNumber.filter(sameAsNewBookingNumber).isPresent()) {
            telemetryClient.trackEvent("P2PImprisonmentStatusBookingNumberAlreadySet", telemetryProperties, null);
            return ConvictionTransformer.custodyOf(event.getDisposal().getCustody());
        } else {
            final var eventName = maybeExistingBookingNumber.isPresent() ? "P2PImprisonmentStatusBookingNumberUpdated" : "P2PImprisonmentStatusBookingNumberInserted";
            telemetryClient.trackEvent(eventName, telemetryProperties, null);
            return ConvictionTransformer
                    .custodyOf(updateBookingNumberFor(offender, event, updateCustodyBookingNumber.getBookingNumber()).getDisposal().getCustody());
        }
    }


    @Transactional(readOnly = true)
    public Custody getCustodyByBookNumber(String nomsNumber, String bookingNumber) {
        final var offender = offenderRepository.findByNomsNumber(nomsNumber)
                .orElseThrow(() -> new NotFoundException(String.format("offender with nomsNumber %s not found", nomsNumber)));
        try {
            final var event = convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(offender.getOffenderId(), bookingNumber)
                    .orElseThrow(() -> new NotFoundException(String.format("conviction with bookNumber %s not found", bookingNumber)));
            return ConvictionTransformer.custodyOf(event.getDisposal().getCustody());
        } catch (ConvictionService.DuplicateActiveCustodialConvictionsException e) {
            throw new NotFoundException(String.format("no single conviction with bookingNumber %s found, instead %d duplicates found", bookingNumber, e.getConvictionCount()));
        }
    }

    @Transactional(readOnly = true)
    public Custody getCustodyByConvictionId(String crn, Long convictionId) {
        final var offender = offenderRepository.findByCrn(crn)
                .orElseThrow(() -> new NotFoundException(String.format("offender with crn %s not found", crn)));
        return Optional.ofNullable(convictionService.convictionFor(offender.getOffenderId(), convictionId)
                .orElseThrow(() -> new NotFoundException(String.format("conviction with convictionId %d not found", convictionId))).getCustody())
                .orElseThrow(() -> new BadRequestException(String.format("The conviction with convictionId %d is not a custodial sentence", convictionId)));
    }

    private Event updateBookingNumberFor(Offender offender, Event event, String bookingNumber) {
        if (updateBookingNumberFeatureSwitch) {
            event.getDisposal().getCustody().setPrisonerNumber(bookingNumber);
            offenderPrisonerService.refreshOffenderPrisonersFor(offender);
            spgNotificationService.notifyUpdateOfCustody(offender, event);
            contactService.addContactForBookingNumberUpdate(offender, event);
        } else {
            log.warn("Update booking number will be ignored, this feature is switched off ");
        }
        return event;
    }


    private boolean currentlyAtDifferentInstitution(Event event, RInstitution institution) {
        return Optional.ofNullable(event.getDisposal().getCustody().getInstitution())
                .map(currentInstitution -> !currentInstitution.equals(institution))
                .orElse(true);
    }

    private boolean isInCustodyOrAboutToStartACustodySentence(uk.gov.justice.digital.delius.jpa.standard.entity.Custody custody) {
        return custody.isAboutToEnterCustody() || custody.isInCustody();
    }

    private Event updateInstitutionOnEvent(Offender offender, Event event, RInstitution institution) {
        if (updateCustodyFeatureSwitch) {
            final var custody = event.getDisposal().getCustody();
            custody.setInstitution(institution);
            custody.setLocationChangeDate(LocalDate.now());
            savePrisonLocationChangeCustodyHistoryEvent(offender, custody, institution);
            if (custody.isAboutToEnterCustody()) {
                custody.setStatusChangeDate(LocalDate.now());
                custody.setCustodialStatus(referenceDataService.getInCustodyCustodyStatus());
                saveCustodyStatusChangeCustodyHistoryEvent(offender, custody);
            }
            spgNotificationService.notifyUpdateOfCustodyLocationChange(offender, event);
            spgNotificationService.notifyUpdateOfCustody(offender, event);
            updatePrisonOffenderManager(offender, institution);
            contactService.addContactForPrisonLocationChange(offender, event);
            return event;
        } else {
            log.warn("Update institution will be ignored, this feature is switched off ");
            return event;
        }
    }

    private void updatePrisonOffenderManager(Offender offender, RInstitution institution) {
        if (!offenderManagerService.isPrisonOffenderManagerAtInstitution(offender, institution)) {
            offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(offender, institution);
        }
    }

    private void savePrisonLocationChangeCustodyHistoryEvent(Offender offender, uk.gov.justice.digital.delius.jpa.standard.entity.Custody custody, RInstitution institution) {
        final var history = CustodyHistory
                .builder()
                .custody(custody)
                .offender(offender)
                .detail(institution.getDescription())
                .when(LocalDate.now())
                .custodyEventType(referenceDataService.getPrisonLocationChangeCustodyEvent())
                .build();
        custodyHistoryRepository.save(history);
    }

    private void saveCustodyStatusChangeCustodyHistoryEvent(Offender offender, uk.gov.justice.digital.delius.jpa.standard.entity.Custody custody) {
        final var history = CustodyHistory
                .builder()
                .custody(custody)
                .offender(offender)
                .detail("DSS auto update in custody")
                .when(LocalDate.now())
                .custodyEventType(referenceDataService.getCustodyStatusChangeCustodyEvent())
                .build();
        custodyHistoryRepository.save(history);
    }

    private Either<PrisonLocationUpdateError, Offender> findByNomsNumber(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber)
                .map((Function<Offender, Either<PrisonLocationUpdateError, Offender>>) Either::right)
                .orElseGet(() -> Either.left(new PrisonLocationUpdateError(PrisonLocationUpdateError.Reason.OffenderNotFound, String.format("offender with nomsNumber %s not found", nomsNumber))));
    }

    private Either<PrisonLocationUpdateError, Event> getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(Long offenderId, String bookingNumber) {
        try {
            return convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(offenderId, bookingNumber)
                    .map((Function<Event, Either<PrisonLocationUpdateError, Event>>) Either::right)
                    .orElseGet(() -> Either.left(new PrisonLocationUpdateError(PrisonLocationUpdateError.Reason.ConvictionNotFound, String.format("conviction with bookingNumber %s not found", bookingNumber))));
        } catch (ConvictionService.DuplicateActiveCustodialConvictionsException e) {
            return Either.left(new PrisonLocationUpdateError(PrisonLocationUpdateError.Reason.MultipleCustodialSentences, String.format("no single conviction with bookingNumber %s found, instead %d duplicates found", bookingNumber, e.getConvictionCount())));
        }
    }

    private Either<PrisonLocationUpdateError, Event> getSingleActiveCustodialEvent(Offender offender) {
        final var events = convictionService.getAllActiveCustodialEvents(offender.getOffenderId());
        switch (events.size()) {
            case 0:
                return Either.left(new PrisonLocationUpdateError(PrisonLocationUpdateError.Reason.ConvictionNotFound, String
                        .format("No active custodial events found for offender %s", offender.getCrn())));
            case 1:
                return Either.right(events.get(0));
            default:
                return Either.left(new PrisonLocationUpdateError(PrisonLocationUpdateError.Reason.MultipleCustodialSentences, String
                        .format("Multiple active custodial events found for offender %s. %d found", offender.getCrn(), events.size())));
        }
    }

    private Either<PrisonLocationUpdateError, Event> isInCustodyOrAboutToStartACustodySentence(Event event) {
        return isInCustodyOrAboutToStartACustodySentence(event.getDisposal().getCustody())
                ? Either.right(event)
                : Either.left(new PrisonLocationUpdateError(PrisonLocationUpdateError.Reason.CustodialSentenceNotFoundInCorrectState, String.format("conviction with custodial status of In Custody or Sentenced Custody not found. Status was %s", event.getDisposal().getCustody().getCustodialStatus())));
    }

    private Either<PrisonLocationUpdateError, RInstitution> findByNomisCdeCode(String nomisCdeCode) {
        return institutionRepository.findByNomisCdeCode(nomisCdeCode)
                .map((Function<RInstitution, Either<PrisonLocationUpdateError, RInstitution>>) Either::right)
                .orElseGet(() -> Either.left(new PrisonLocationUpdateError(PrisonLocationUpdateError.Reason.TransferPrisonNotFound, String.format("prison institution with nomis code  %s not found", nomisCdeCode))));
    }

    private Either<PrisonLocationUpdateError, PrisonLocationUpdateSuccess> updateInstitutionWhenDifferent(Offender offender, Event event, RInstitution institution) {
        if (currentlyAtDifferentInstitution(event, institution)) {
            return Either.right(PrisonLocationUpdateSuccess.updated(ConvictionTransformer.custodyOf(updateInstitutionOnEvent(offender, event, institution)
                    .getDisposal()
                    .getCustody())));

        } else {
            return Either.right(PrisonLocationUpdateSuccess.noUpdateRequired(ConvictionTransformer.custodyOf(event
                    .getDisposal()
                    .getCustody())));
        }
    }

    @Data
    static class PrisonLocationUpdateError {
        enum Reason {
            TransferPrisonNotFound,
            CustodialSentenceNotFoundInCorrectState,
            ConvictionNotFound,
            MultipleCustodialSentences,
            OffenderNotFound
        }
        final private Reason reason;
        final private String message;
    }
    @Data
    static class PrisonLocationUpdateSuccess {
        static PrisonLocationUpdateSuccess updated(Custody custody) {
            return new PrisonLocationUpdateSuccess(Outcome.Updated, custody);
        }
        static PrisonLocationUpdateSuccess noUpdateRequired(Custody custody) {
            return new PrisonLocationUpdateSuccess(Outcome.NoUpdateRequired, custody);
        }

        enum Outcome {
            Updated,
            NoUpdateRequired
        }
        final private Outcome outcome;
        final private Custody custody;
    }
}
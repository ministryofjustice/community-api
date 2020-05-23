package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public Custody updateCustody(final String nomsNumber,
                                 final String bookingNumber,
                                 final UpdateCustody updateCustody) {
        final var maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);
        final var telemetryProperties = Map.of("offenderNo", nomsNumber,
                "bookingNumber", bookingNumber,
                "toAgency", updateCustody.getNomsPrisonInstitutionCode());

        return maybeOffender.map(offender -> {
            try {
                final var maybeEvent = convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(offender.getOffenderId(), bookingNumber);
                return maybeEvent.map(event -> {
                    if (isInCustodyOrAboutToStartACustodySentence(event.getDisposal().getCustody())) {
                        final var maybeInstitution = institutionRepository.findByNomisCdeCode(updateCustody.getNomsPrisonInstitutionCode());
                        return maybeInstitution.map(institution -> {
                            if (currentlyAtDifferentInstitution(event, institution)) {
                                telemetryClient.trackEvent("P2PTransferPrisonUpdated", telemetryProperties, null);
                                return ConvictionTransformer
                                        .custodyOf(updateInstitutionOnEvent(offender, event, institution).getDisposal().getCustody());
                            } else {
                                telemetryClient.trackEvent("P2PTransferPrisonUpdateIgnored", telemetryProperties, null);
                                return ConvictionTransformer.custodyOf(event.getDisposal().getCustody());
                            }
                        }).orElseThrow(() -> {
                            telemetryClient.trackEvent("P2PTransferPrisonNotFound", telemetryProperties, null);
                            return new NotFoundException(String.format("prison institution with nomis code  %s not found", updateCustody.getNomsPrisonInstitutionCode()));
                        });
                    } else {
                        telemetryClient.trackEvent("P2PTransferPrisonUpdateIgnored", telemetryProperties, null);
                        throw new NotFoundException(String.format("conviction with custodial status of In Custody or Sentenced Custody not found. Status was %s", event.getDisposal().getCustody().getCustodialStatus()));
                    }
                }).orElseThrow(() -> {
                    telemetryClient.trackEvent("P2PTransferBookingNumberNotFound", telemetryProperties, null);
                    return new NotFoundException(String.format("conviction with bookingNumber %s not found", bookingNumber));
                });
            } catch (ConvictionService.DuplicateConvictionsForBookingNumberException e) {
                telemetryClient.trackEvent("P2PTransferBookingNumberHasDuplicates", telemetryProperties, null);
                throw new NotFoundException(String.format("no single conviction with bookingNumber %s found, instead %d duplicates found", bookingNumber, e.getConvictionCount()));
            }
        }).orElseThrow(() -> {
            telemetryClient.trackEvent("P2PTransferOffenderNotFound", telemetryProperties, null);
            return new NotFoundException(String.format("offender with nomsNumber %s not found", nomsNumber));
        });
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
}
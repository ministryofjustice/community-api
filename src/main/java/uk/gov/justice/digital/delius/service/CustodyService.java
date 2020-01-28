package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionRepository;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;

import java.util.Map;

@Service
@Slf4j
public class CustodyService {
    private final Boolean updateCustodyFeatureSwitch;
    private final TelemetryClient telemetryClient;
    private final OffenderService offenderService;
    private final ConvictionService convictionService;
    private final InstitutionRepository institutionRepository;
    private final ConvictionTransformer convictionTransformer;

    public CustodyService(
            @Value("${features.noms.update.custody}")
            Boolean updateCustodyFeatureSwitch,
            TelemetryClient telemetryClient,
            OffenderService offenderService,
            ConvictionService convictionService,
            InstitutionRepository institutionRepository,
            ConvictionTransformer convictionTransformer) {
        this.updateCustodyFeatureSwitch = updateCustodyFeatureSwitch;
        this.telemetryClient = telemetryClient;
        this.offenderService = offenderService;
        this.convictionService = convictionService;
        this.institutionRepository = institutionRepository;
        this.convictionTransformer = convictionTransformer;
    }

    @Transactional
    public Custody updateCustody(final String nomsNumber,
                                 final String bookingNumber,
                                 final UpdateCustody updateCustody) {
        final var maybeOffender = offenderService.getOffenderByNomsNumber(nomsNumber);
        final var telemetryProperties = Map.of("offenderNo", nomsNumber,
                "bookingNumber", bookingNumber,
                "toAgency", updateCustody.getNomsPrisonInstitutionCode());

        return maybeOffender.map(offender -> {
            try {
                final var maybeEvent = convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(offender.getOffenderId(), bookingNumber);
                return maybeEvent.map(event -> {
                    final var maybeInstitution = institutionRepository.findByNomisCdeCode(updateCustody.getNomsPrisonInstitutionCode());
                    return maybeInstitution.map(institution -> {
                        telemetryClient.trackEvent("P2PTransferPrisonUpdated", telemetryProperties, null);
                        return convictionTransformer.custodyOf(updateInstitutionOnEvent(event, institution).getDisposal().getCustody());
                    }).orElseThrow(() -> {
                        telemetryClient.trackEvent("P2PTransferPrisonNotFound", telemetryProperties, null);
                        return new NotFoundException(String.format("prison institution with nomis code  %s not found", updateCustody.getNomsPrisonInstitutionCode()));
                    });
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

    private Event updateInstitutionOnEvent(Event event, RInstitution institution) {
        if (updateCustodyFeatureSwitch) {
            event.getDisposal().getCustody().setInstitution(institution);
            return event;
        } else {
            log.warn("Update institution will be ignored, this feature is switched off ");
            return event;
        }
    }
}
package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTier;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTierId;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ManagementTierRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class TierService {

    private static final long INSERTED = 1L;
    private final ManagementTierRepository managementTierRepository;
    private final StandardReferenceRepository standardReferenceRepository;
    private final TelemetryClient telemetryClient;
    private final OffenderRepository offenderRepository;


    @Transactional
    public void updateTier(String crn, String tier) {
        Map<String, String> telemetryProperties = Map.of("crn", crn, "tier", tier);
        final Long offenderId = offenderRepository.findByCrn(crn).map(Offender::getOffenderId).orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureOffenderNotFound", String.format("Offender with CRN %s not found", crn)));

        final StandardReference updatedTier = standardReferenceRepository.findByCodeAndCodeSetName(String.format("U%s",tier), "TIER").orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureTierNotFound", String.format("Tier %s not found", tier)));

        final StandardReference changeReason = standardReferenceRepository.findByCodeAndCodeSetName("ATS", "TIER CHANGE REASON").orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureTierChangeReasonNotFound", "Tier change reason ATS not found"));


        ManagementTier newTier = ManagementTier
            .builder()
            .id(ManagementTierId.builder().offenderId(offenderId).tier(updatedTier).dateChanged(LocalDateTime.now()).build())
            .tierChangeReason(changeReason)
            .rowVersion(INSERTED)
            .partitionAreaId(0L)
            .softDeleted(0)
            .build();

        managementTierRepository.save(newTier);

        telemetryClient.trackEvent("TierUpdateSuccess", telemetryProperties, null);
    }

    private NotFoundException logAndThrow(Map<String, String> telemetryProperties, String event, String exceptionReason) {
        telemetryClient.trackEvent(event, telemetryProperties, null);
        return new NotFoundException(exceptionReason);
    }
}

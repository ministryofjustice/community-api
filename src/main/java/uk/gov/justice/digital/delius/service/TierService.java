package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTier;
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

    private final ManagementTierRepository managementTierRepository;
    private final StandardReferenceRepository standardReferenceRepository;
    private final TelemetryClient telemetryClient;
    private final OffenderRepository offenderRepository;


    @Transactional
    public void updateTier(String crn, String tier) {
        Map<String, String> telemetryProperties = Map.of("crn", crn, "tier", tier);
        final Long offenderId = offenderRepository.findByCrn(crn).map(Offender::getOffenderId).orElseThrow(() -> {
            telemetryClient.trackEvent("TierUpdateFailureOffenderNotFound", telemetryProperties, null);
            return new NotFoundException(String.format("Offender with CRN %s not found", crn));
        });

        StandardReference updatedTier = standardReferenceRepository.findByCodeAndCodeSetName(String.format("U%s",tier), "TIER").orElseThrow(() -> {
            telemetryClient.trackEvent("TierUpdateFailureTierNotFound", telemetryProperties, null);
            return new NotFoundException(String.format("Tier %s not found", tier));
        });

        StandardReference changeReason = standardReferenceRepository.findByCodeAndCodeSetName("ATS", "TIER CHANGE REASON").orElseThrow(() -> {
            telemetryClient.trackEvent("TierUpdateFailureTierChangeReasonNotFound", telemetryProperties, null);
            return new NotFoundException("Tier change reason ATS not found");
        });


        ManagementTier newTier = ManagementTier
            .builder()
            .offenderId(offenderId)
            .tier(updatedTier)
            .tierChangeReason(changeReason)
            .rowVersion(1L)
            .partitionAreaId(0L)
            .softDeleted(0)
            .dateChanged(LocalDateTime.now())
            .build();

        managementTierRepository.save(newTier);

        telemetryClient.trackEvent("TierUpdateSuccess", telemetryProperties, null);
    }
}

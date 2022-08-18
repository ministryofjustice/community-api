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
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.ManagementTierRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class TierService {

    private final ManagementTierRepository managementTierRepository;
    private final TelemetryClient telemetryClient;
    private final OffenderRepository offenderRepository;
    private final ReferenceDataService referenceDataService;
    private final ContactService contactService;
    private final StaffRepository staffRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public void updateTier(String crn, String tier) {
        final var tierWithUPrefix = tierWithUPrefix(tier);
        final var telemetryProperties = Map.of("crn", crn, "tier", tierWithUPrefix);
        final var offender = getOffender(crn, telemetryProperties);
        final var offenderId = offender.getOffenderId();
        final var changeReason = getChangeReason(telemetryProperties);
        final var currentTier = managementTierRepository.findFirstByIdOffenderIdOrderByIdDateChangedDesc(offenderId);
        final var updatedTier = getUpdatedTier(tier, tierWithUPrefix, telemetryProperties);

        if(currentTier == null || !currentTier.getId().getTier().equals(updatedTier)) {
            writeTierUpdate(updatedTier, offenderId, changeReason);
            writeContact(offender, changeReason, updatedTier, telemetryProperties);
            telemetryClient.trackEvent("TierUpdateSuccess", telemetryProperties, null);
        }
    }

    private void writeContact(Offender offender, StandardReference changeReason, StandardReference updatedTier, Map<String, String> telemetryProperties) {
        final var areaCode = getAreaCode(offender, telemetryProperties);
        final var staff = getStaff(areaCode, telemetryProperties);
        final var team = getTeam(areaCode, telemetryProperties);
        contactService.addContactForTierUpdate(offender.getOffenderId(), LocalDateTime.now(), updatedTier.getCodeDescription(), changeReason.getCodeDescription(), staff, team);
    }

    private String getAreaCode(Offender offender, Map<String, String> telemetryProperties) {
        final var offenderManager = getOffenderManager(offender, telemetryProperties);
        return offenderManager.getProbationArea().getCode();
    }

    private Team getTeam(String areaCode, Map<String, String> telemetryProperties) {
        final var teamCode = String.format("%sUTS", areaCode);
        return teamRepository.findByCode(teamCode).orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureTeamNotFound", String.format("Could not find team with code %s", teamCode)));
    }

    private Staff getStaff(String areaCode, Map<String, String> telemetryProperties) {
        final var staffCode = String.format("%sUTSO", areaCode);
        return staffRepository.findByOfficerCode(staffCode).orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureStaffNotFound", String.format("Could not find staff with officer code %s", staffCode)));
    }

    private OffenderManager getOffenderManager(Offender offender, Map<String, String> telemetryProperties) {
        return offender.getActiveCommunityOffenderManager().orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureActiveCommunityOffenderManagerNotFound", String.format("Could not find active community manager for crn %s", offender.getCrn())));
    }

    private StandardReference getUpdatedTier(String tier, String tierWithUPrefix, Map<String, String> telemetryProperties) {
        return referenceDataService.getTier(tierWithUPrefix).orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureTierNotFound", String.format("Tier %s not found", tier)));
    }

    private StandardReference getChangeReason(Map<String, String> telemetryProperties) {
        return referenceDataService.getAtsTierChangeReason().orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureTierChangeReasonNotFound", "Tier change reason ATS not found"));
    }

    private Offender getOffender(String crn, Map<String, String> telemetryProperties) {
        return offenderRepository.findByCrnAndSoftDeletedFalse(crn).orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureOffenderNotFound", String.format("Offender with CRN %s not found", crn)));
    }

    private void writeTierUpdate(StandardReference tier, Long offenderId, StandardReference changeReason) {

        ManagementTier newTier = ManagementTier
            .builder()
            .id(ManagementTierId
                .builder()
                .offenderId(offenderId)
                .tier(tier)
                .dateChanged(LocalDateTime.now())
                .build())
            .tierChangeReason(changeReason)
            .build();

        managementTierRepository.save(newTier);
    }

    private String tierWithUPrefix(String tier) {
        return String.format("U%s", tier);
    }

    private NotFoundException logAndThrow(Map<String, String> telemetryProperties, String event, String exceptionReason) {
        telemetryClient.trackEvent(event, telemetryProperties, null);
        return new NotFoundException(exceptionReason);
    }
}

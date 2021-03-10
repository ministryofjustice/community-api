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
    private final SpgNotificationService spgNotificationService;


    @Transactional
    public void updateTier(String crn, String tier) {
        final var tierWithUPrefix = tierWithUPrefix(tier);
        final var telemetryProperties = Map.of("crn", crn, "tier", tierWithUPrefix);
        final var offender = getOffender(crn, telemetryProperties);

        final var offenderId = offender.getOffenderId();
        final var changeReason = referenceDataService.getAtsTierChangeReason().orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureTierChangeReasonNotFound", "Tier change reason ATS not found"));

        String tierDescription = writeTierUpdate(tierWithUPrefix, telemetryProperties, offenderId, changeReason);

        OffenderManager offenderManager = offender.getActiveCommunityOffenderManager().orElseThrow(() ->  logAndThrow(telemetryProperties, "TierUpdateFailureActiveCommunityOffenderManagerNotFound", String.format("Could not find active community manager for crn %s", crn)));
        Staff staff;
        Team team;
        try {
            String areaCode = offenderManager.getProbationArea().getCode();
            String staffCode = String.format("%sUTSO", areaCode);
            staff = staffRepository.findByOfficerCode(staffCode).orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureStaffNotFound", String.format("Could not find staff with officer code %s", staffCode)));
            String teamCode = String.format("%sUTS", areaCode);
            team = teamRepository.findByCode(teamCode).orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureTeamNotFound", String.format("Could not find team with code %s", teamCode)));
        } catch (NotFoundException e) {
            staff = offenderManager.getStaff();
            team = offenderManager.getTeam();
        }

        contactService.addContactForTierUpdate(offenderId, LocalDateTime.now(), tierDescription, changeReason.getCodeDescription(), staff, team);
        spgNotificationService.notifyUpdateOfOffender(offender);
        telemetryClient.trackEvent("TierUpdateSuccess", telemetryProperties, null);
    }

    private Offender getOffender(String crn, Map<String, String> telemetryProperties) {
        return offenderRepository.findByCrn(crn).orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureOffenderNotFound", String.format("Offender with CRN %s not found", crn)));
    }

    private String writeTierUpdate(String tier, Map<String, String> telemetryProperties, Long offenderId, StandardReference changeReason) {
        final var updatedTier = referenceDataService.getTier(tier).orElseThrow(() -> logAndThrow(telemetryProperties, "TierUpdateFailureTierNotFound", String.format("Tier %s not found", tier)));

        ManagementTier newTier = ManagementTier
            .builder()
            .id(ManagementTierId
                .builder()
                .offenderId(offenderId)
                .tier(updatedTier)
                .dateChanged(LocalDateTime.now())
                .build())
            .tierChangeReason(changeReason)
            .build();

        managementTierRepository.save(newTier);
        return updatedTier.getCodeDescription();
    }

    private String tierWithUPrefix(String tier) {
        return String.format("U%s", tier);
    }

    private NotFoundException logAndThrow(Map<String, String> telemetryProperties, String event, String exceptionReason) {
        telemetryClient.trackEvent(event, telemetryProperties, null);
        return new NotFoundException(exceptionReason);
    }
}

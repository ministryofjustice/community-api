package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.jpa.standard.entity.Borough;
import uk.gov.justice.digital.delius.jpa.standard.entity.District;
import uk.gov.justice.digital.delius.jpa.standard.entity.LocalDeliveryUnit;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StaffTeam;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.BoroughRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.DistrictRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.LocalDeliveryUnitRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffTeamRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TeamService {
    private static final String POM_TEAM_SUFFIX = "POM";
    static final String UNALLOCATED_TEAM_SUFFIX = "ALL";
    private static final String POM_DESCRIPTION = "Prison Offender Managers";

    private final TeamRepository teamRepository;
    private final LocalDeliveryUnitRepository localDeliveryUnitRepository;
    private final DistrictRepository districtRepository;
    private final BoroughRepository boroughRepository;
    private final StaffTeamRepository staffTeamRepository;
    private final TelemetryClient telemetryClient;
    private final StaffService staffService;

    @Transactional
    public Team findOrCreatePrisonOffenderManagerTeamInArea(ProbationArea probationArea) {
        final String teamCode = String.format("%s%s", probationArea.getCode(), POM_TEAM_SUFFIX);
        return teamRepository.findActiveByCode(teamCode)
                .orElseGet(() -> createPOMTeamInArea(teamCode, probationArea));
    }

    Optional<Team> findUnallocatedTeam(uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea probationArea) {
        final String teamCode = String.format("%s%s", probationArea.getCode(), UNALLOCATED_TEAM_SUFFIX);
        return teamRepository.findActiveByCode(teamCode);
    }

    @Transactional
    public void addStaffToTeam(Staff staff, Team team) {
        staffTeamRepository.save(
                StaffTeam
                        .builder ()
                        .teamId(team.getTeamId())
                        .staffId(staff.getStaffId())
                        .build()
        );
    }

    private Team createPOMTeamInArea(String code, ProbationArea probationArea) {
        final var team = Team
                .builder()
                .code(code)
                .description(POM_DESCRIPTION)
                .probationArea(probationArea)
                .district(findOrCreatePOMDistrictInArea(code, probationArea))
                .localDeliveryUnit(findOrCreatePOMLDUInArea(code, probationArea))
                .privateFlag(probationArea.getPrivateSector())
                .unpaidWorkTeam("N")
                .build();
        probationArea.getTeams().add(team);
        telemetryClient.trackEvent("POMTeamCreated", Map.of("probationArea", probationArea.getCode(), "code", code), null);

        return teamRepository.save(team);
    }

    private LocalDeliveryUnit findOrCreatePOMLDUInArea(String code, ProbationArea probationArea) {
        return localDeliveryUnitRepository.findByCode(code)
                .orElseGet(() -> createPOMLDUInArea(code, probationArea));
    }

    private LocalDeliveryUnit createPOMLDUInArea(String code, ProbationArea probationArea) {
        telemetryClient.trackEvent("POMTeamTypeCreated", Map.of("probationArea", probationArea.getCode(), "code", code), null);

        return localDeliveryUnitRepository.save(
                LocalDeliveryUnit
                        .builder()
                        .code(code)
                        .description(POM_DESCRIPTION)
                        .probationArea(probationArea)
                        .build()
        );
    }

    private District findOrCreatePOMDistrictInArea(String code, ProbationArea probationArea) {
        return districtRepository.findByCode(code)
                .orElseGet(() -> createPOMDistrictInArea(code, probationArea));
    }

    private District createPOMDistrictInArea(String code, ProbationArea probationArea) {
        telemetryClient.trackEvent("POMLDUCreated", Map.of("probationArea", probationArea.getCode(), "code", code), null);

        return districtRepository.save(
                District
                        .builder()
                        .code(code)
                        .description(POM_DESCRIPTION)
                        .borough(findOrCreatePOMBoroughInArea(code, probationArea))
                        .build()
        );
    }

    private Borough findOrCreatePOMBoroughInArea(String code, ProbationArea probationArea) {
        return boroughRepository.findActiveByCode(code)
                .orElseGet(() -> createPOMBoroughInArea(code, probationArea));
    }

    private Borough createPOMBoroughInArea(String code, ProbationArea probationArea) {
        telemetryClient.trackEvent("POMClusterCreated", Map.of("probationArea", probationArea.getCode(), "code", code), null);

        return boroughRepository.save(
                Borough
                        .builder()
                        .code(code)
                        .description(POM_DESCRIPTION)
                        .probationArea(probationArea)
                        .build()
        );
    }
}

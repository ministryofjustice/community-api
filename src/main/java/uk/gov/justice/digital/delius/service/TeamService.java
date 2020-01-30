package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.*;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TeamService {
    private static final String POM_TEAM_SUFFIX = "POM";
    private static final String UNALLOCATED_TEAM_SUFFIX = "ALL";
    private static final String POM_DESCRIPTION_SUFFIX = "Prison Offender Managers";

    private final TeamRepository teamRepository;
    private final LocalDeliveryUnitRepository localDeliveryUnitRepository;
    private final DistrictRepository districtRepository;
    private final BoroughRepository boroughRepository;
    private final StaffTeamRepository staffTeamRepository;


    @Transactional
    public Team findOrCreatePrisonOffenderManagerTeamInArea(ProbationArea probationArea) {
        final String teamCode = String.format("%s%s", probationArea.getCode(), POM_TEAM_SUFFIX);
        return teamRepository.findByCode(teamCode)
                .orElseGet(() -> createPOMTeamInArea(teamCode, probationArea));
    }

    Optional<Team> findUnallocatedTeam(uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea probationArea) {
        final String teamCode = String.format("%s%s", probationArea.getCode(), UNALLOCATED_TEAM_SUFFIX);
        return teamRepository.findByCode(teamCode);
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
                .description(POM_DESCRIPTION_SUFFIX)
                .probationArea(probationArea)
                .district(findOrCreatePOMDistrictInArea(code, probationArea))
                .localDeliveryUnit(findOrCreatePOMLDUInArea(code, probationArea))
                .privateFlag(probationArea.getPrivateSector())
                .unpaidWorkTeam("Y")  // same as case notes team but seems odd
                .build();
        probationArea.getTeams().add(team);
        return teamRepository.save(team);
    }

    private LocalDeliveryUnit findOrCreatePOMLDUInArea(String code, ProbationArea probationArea) {
        return localDeliveryUnitRepository.findByCode(code)
                .orElseGet(() -> createPOMLDUInArea(code, probationArea));
    }

    private LocalDeliveryUnit createPOMLDUInArea(String code, ProbationArea probationArea) {
        return localDeliveryUnitRepository.save(
                LocalDeliveryUnit
                        .builder()
                        .code(code)
                        .description(POM_DESCRIPTION_SUFFIX)
                        .probationArea(probationArea)
                        .build()
        );
    }

    private District findOrCreatePOMDistrictInArea(String code, ProbationArea probationArea) {
        return districtRepository.findByCode(code)
                .orElseGet(() -> createPOMDistrictInArea(code, probationArea));
    }

    private District createPOMDistrictInArea(String code, ProbationArea probationArea) {
        return districtRepository.save(
                District
                        .builder()
                        .code(code)
                        .description(POM_DESCRIPTION_SUFFIX)
                        .borough(findOrCreatePOMBoroughInArea(code, probationArea))
                        .build()
        );
    }

    private Borough findOrCreatePOMBoroughInArea(String code, ProbationArea probationArea) {
        return boroughRepository.findByCode(code)
                .orElseGet(() -> createPOMBoroughInArea(code, probationArea));
    }

    private Borough createPOMBoroughInArea(String code, ProbationArea probationArea) {
        return boroughRepository.save(
                Borough
                        .builder()
                        .code(code)
                        .description(POM_DESCRIPTION_SUFFIX)
                        .probationArea(probationArea)
                        .build()
        );
    }
}

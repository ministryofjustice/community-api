package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.data.api.OfficeLocation;
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
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffTeamRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;
import uk.gov.justice.digital.delius.transformers.StaffTransformer;
import uk.gov.justice.digital.delius.transformers.TeamTransformer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@AllArgsConstructor
@Slf4j
public class TeamService {
    private static final String POM_TEAM_SUFFIX = "POM";
    private static final String UNALLOCATED_TEAM_SUFFIX = "ALL";
    private static final String POM_DESCRIPTION = "Prison Offender Managers";

    private final TeamRepository teamRepository;
    private final LocalDeliveryUnitRepository localDeliveryUnitRepository;
    private final DistrictRepository districtRepository;
    private final BoroughRepository boroughRepository;
    private final StaffTeamRepository staffTeamRepository;
    private final ProbationAreaRepository probationAreaRepository;
    private final TelemetryClient telemetryClient;
    private final StaffService staffService;

    @Transactional
    public Team findOrCreatePrisonOffenderManagerTeamInArea(ProbationArea probationArea) {
        final String teamCode = String.format("%s%s", probationArea.getCode(), POM_TEAM_SUFFIX);
        return teamRepository.findByCode(teamCode)
                .orElseGet(() -> createPOMTeamInArea(teamCode, probationArea));
    }

    public List<OfficeLocation> getAllOfficeLocations(String teamCode) {
        final var team = teamRepository.findActiveWithActiveOfficeLocationByCode(teamCode)
            .orElseThrow(() -> new NotFoundException(format("team '%s' does not exist", teamCode)));

        return team.getOfficeLocations()
            .stream()
            .map(x -> OfficeLocation.builder()
                .code(x.getCode())
                .description(x.getDescription())
                .buildingName(x.getBuildingName())
                .buildingNumber(x.getBuildingNumber())
                .streetName(x.getStreetName())
                .townCity(x.getTownCity())
                .county(x.getCounty())
                .postcode(x.getPostcode())
                .build())
            .collect(Collectors.toList());
    }

    private Team createPrisonOffenderManagerTeamInArea(ProbationArea probationArea) {
        log.info(String.format("Creating Prison Offender Manger team for %s", probationArea.getDescription()));
        final String teamCode = String.format("%s%s", probationArea.getCode(), POM_TEAM_SUFFIX);
        return createPOMTeamInArea(teamCode, probationArea);
    }

    private boolean isPOMTeamMissingForArea(ProbationArea probationArea) {
        final String teamCode = String.format("%s%s", probationArea.getCode(), POM_TEAM_SUFFIX);
        return teamRepository.findByCode(teamCode).isEmpty();
    }

    private boolean isPOMTeamMissingUnallocatedStaff(Team team) {
        return staffService.findUnallocatedForTeam(team).isEmpty();
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


    @Transactional
    public List<uk.gov.justice.digital.delius.data.api.Team> createMissingPrisonOffenderManagerTeams() {
        return probationAreaRepository
                        .findAllWithNomsCDECodeExcludeOut()
                        .stream()
                        .filter(this::isPOMTeamMissingForArea)
                        .map(probationArea -> TeamTransformer.teamOf(createPrisonOffenderManagerTeamInArea(probationArea)))
                        .collect(Collectors.toList());
    }

    @Transactional
    public List<StaffHuman> createMissingPrisonOffenderManagerUnallocatedStaff() {
        return probationAreaRepository
            .findAllWithNomsCDECodeExcludeOut()
            .stream()
            .map(this::findOrCreatePrisonOffenderManagerTeamInArea)
            .filter(this::isPOMTeamMissingUnallocatedStaff)
            .map(team -> StaffTransformer.staffOf(createUnallocatedStaffInTeam(team)))
            .collect(Collectors.toList());
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

    private Staff createUnallocatedStaffInTeam(Team team) {
        final var staff = staffService.createUnallocatedStaffInArea(POM_TEAM_SUFFIX, team.getProbationArea());
        addStaffToTeam(staff, team);
        telemetryClient.trackEvent("POMTeamUnallocatedStaffCreated", Map.of("probationArea", team.getProbationArea().getCode(), "code", staff.getOfficerCode()), null);
        return staff;
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
        return boroughRepository.findByCode(code)
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

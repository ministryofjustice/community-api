package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.Borough;
import uk.gov.justice.digital.delius.jpa.standard.entity.District;
import uk.gov.justice.digital.delius.jpa.standard.entity.LocalDeliveryUnit;
import uk.gov.justice.digital.delius.jpa.standard.entity.StaffTeam;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.BoroughRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.DistrictRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.LocalDeliveryUnitRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffTeamRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aDistrict;
import static uk.gov.justice.digital.delius.util.EntityHelper.aLocalDeliveryUnit;
import static uk.gov.justice.digital.delius.util.EntityHelper.aProbationArea;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private LocalDeliveryUnitRepository localDeliveryUnitRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private BoroughRepository boroughRepository;
    @Mock
    private StaffTeamRepository staffTeamRepository;
    @Mock
    private ProbationAreaRepository probationAreaRepository;
    @Captor
    private ArgumentCaptor<StaffTeam> staffTeamArgumentCaptor;
    @Captor
    private ArgumentCaptor<Team> teamArgumentCaptor;
    @Captor
    private ArgumentCaptor<Borough> boroughArgumentCaptor;
    @Captor
    private ArgumentCaptor<District> districtArgumentCaptor;
    @Captor
    private ArgumentCaptor<LocalDeliveryUnit> localDeliveryUnitArgumentCaptor;

    private TeamService teamService;

    @BeforeEach
    public void setup() {
        teamService = new TeamService(teamRepository, localDeliveryUnitRepository, districtRepository, boroughRepository, staffTeamRepository, probationAreaRepository);
    }

    @Test
    public void findOrCreatePrisonOffenderManagerTeamInAreaWillLookupBaseOnPOMCode() {
        when(teamRepository.findByCode(any())).thenReturn(Optional.of(aTeam()));

        assertThat(teamService.findOrCreatePrisonOffenderManagerTeamInArea(
                aProbationArea()
                        .toBuilder()
                        .code("N01")
                        .build())).isNotNull();

        verify(teamRepository).findByCode("N01POM");
    }

    @Test
    public void findUnallocatedTeamInAreaWillLookupBaseOnALLCode() {
        when(teamRepository.findByCode(any())).thenReturn(Optional.of(aTeam()));

        assertThat(teamService.findUnallocatedTeam(
                aProbationArea()
                        .toBuilder()
                        .code("N01")
                        .build())).isNotNull();

        verify(teamRepository).findByCode("N01ALL");
    }

    @Test
    public void findOrCreatePrisonOffenderManagerTeamInAreaWillCreateNewPOMTeamWhenNotFound() {
        when(teamRepository.findByCode(any())).thenReturn(Optional.empty());
        when(districtRepository.findByCode(any())).thenReturn(Optional.of(aDistrict()));
        when(localDeliveryUnitRepository.findByCode(any())).thenReturn(Optional.of(aLocalDeliveryUnit()));

        final var probationArea = aProbationArea()
                .toBuilder()
                .code("N01")
                .teams(new ArrayList<>())
                .build();


        teamService.findOrCreatePrisonOffenderManagerTeamInArea(probationArea);

        verify(teamRepository).save(teamArgumentCaptor.capture());

        assertThat(teamArgumentCaptor.getValue().getCode()).isEqualTo("N01POM");
        assertThat(teamArgumentCaptor.getValue().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(probationArea.getTeams()).hasSize(1);
    }

    @Test
    public void findOrCreatePrisonOffenderManagerTeamInAreaWillCreateDistrictBoroughAndLDUWhenTheyAreNotFound() {
        when(teamRepository.findByCode(any())).thenReturn(Optional.empty());
        when(districtRepository.findByCode(any())).thenReturn(Optional.empty());
        when(localDeliveryUnitRepository.findByCode(any())).thenReturn(Optional.empty());
        when(boroughRepository.findByCode(any())).thenReturn(Optional.empty());

        final var probationArea = aProbationArea()
                .toBuilder()
                .code("N01")
                .teams(new ArrayList<>())
                .build();


        teamService.findOrCreatePrisonOffenderManagerTeamInArea(probationArea);

        verify(boroughRepository).save(boroughArgumentCaptor.capture());
        verify(districtRepository).save(districtArgumentCaptor.capture());
        verify(localDeliveryUnitRepository).save(localDeliveryUnitArgumentCaptor.capture());

        assertThat(boroughArgumentCaptor.getValue().getCode()).isEqualTo("N01POM");
        assertThat(boroughArgumentCaptor.getValue().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(districtArgumentCaptor.getValue().getCode()).isEqualTo("N01POM");
        assertThat(districtArgumentCaptor.getValue().getDescription()).isEqualTo("Prison Offender Managers");
        assertThat(localDeliveryUnitArgumentCaptor.getValue().getCode()).isEqualTo("N01POM");
        assertThat(localDeliveryUnitArgumentCaptor.getValue().getDescription()).isEqualTo("Prison Offender Managers");
    }

    @Test
    public void addStaffToTeamWillCreateLink() {
        final var staff = aStaff()
                .toBuilder()
                .staffId(33L)
                .build();

        final var team = aTeam("N01A5161")
                .toBuilder()
                .teamId(22L)
                .build();


        teamService.addStaffToTeam(staff, team);

        verify(staffTeamRepository).save(staffTeamArgumentCaptor.capture());

        assertThat(staffTeamArgumentCaptor.getValue().getTeamId()).isEqualTo(22L);
        assertThat(staffTeamArgumentCaptor.getValue().getStaffId()).isEqualTo(33L);
    }

    @Nested
    @DisplayName("createMissingPrisonOffenderManagerTeams")
    class CreateMissingPrisonOffenderManagerTeams {
        @BeforeEach
        void setUp() {
            final var probationAreaWithTeamAlready = EntityHelper
                    .aPrisonProbationArea()
                    .toBuilder()
                    .code("A01")
                    .build();
            final var probationAreaWithMissingTeam = EntityHelper
                    .aPrisonProbationArea()
                    .toBuilder()
                    .code("Z01")
                    .build();
            when(probationAreaRepository.findAllWithNomsCDECodeExcludeOut()).thenReturn(List.of(probationAreaWithTeamAlready, probationAreaWithMissingTeam));

            when(teamRepository.findByCode("A01POM")).thenReturn(Optional.of(EntityHelper.aTeam("A01POM")));
            when(teamRepository.findByCode("Z01POM")).thenReturn(Optional.empty());
            when(districtRepository.findByCode("Z01POM")).thenReturn(Optional.empty());
            when(boroughRepository.findByCode("Z01POM")).thenReturn(Optional.empty());
            when(localDeliveryUnitRepository.findByCode("Z01POM")).thenReturn(Optional.empty());

            when(teamRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(districtRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(boroughRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(localDeliveryUnitRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        }

        @Test
        @DisplayName("Will create any missing teams")
        void willCreateTheMissingTeam() {
            final var createdTeams = teamService.createMissingPrisonOffenderManagerTeams();
            assertThat(createdTeams.getTeams()).hasSize(1);

            verify(teamRepository).save(teamArgumentCaptor.capture());
            final var newTeam = teamArgumentCaptor.getValue();

            assertThat(newTeam.getCode()).isEqualTo("Z01POM");
            assertThat(newTeam.getDescription()).isEqualTo("Prison Offender Managers");
            assertThat(newTeam.getUnpaidWorkTeam()).isEqualTo("N");
        }

        @Test
        @DisplayName("Will create any district, ldu and borough for new teams")
        void willCreateTheDistrictLDUAndBorough() {
            teamService.createMissingPrisonOffenderManagerTeams();

            verify(boroughRepository).save(any());
            verify(districtRepository).save(any());
            verify(localDeliveryUnitRepository).save(any());
        }
    }
}
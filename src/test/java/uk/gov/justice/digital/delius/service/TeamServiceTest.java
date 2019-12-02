package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.*;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setup() {
        teamService = new TeamService(teamRepository, localDeliveryUnitRepository, districtRepository, boroughRepository, staffTeamRepository);
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
}
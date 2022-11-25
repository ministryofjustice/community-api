package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.justice.digital.delius.data.api.ManagedEventId;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;
import uk.gov.justice.digital.delius.jpa.standard.entity.Caseload;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.CaseloadRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.data.api.CaseloadRole.OFFENDER_MANAGER;
import static uk.gov.justice.digital.delius.data.api.CaseloadRole.ORDER_SUPERVISOR;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;

@ExtendWith(MockitoExtension.class)
public class CaseloadServiceTest {

    @InjectMocks
    private CaseloadService caseloadService;

    @Mock
    private CaseloadRepository caseloadRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private TeamRepository teamRepository;

    @Test
    public void whenStaffDoesntExist_thenReturnEmpty() {
        when(staffRepository.existsById(1L)).thenReturn(false);
        assertThat(caseloadService.getCaseloadByStaffIdentifier(1L)).isNotPresent();
    }

    @Test
    public void whenCaseloadIsReturnedFromRepository_thenMapAndReturnIt() {
        when(staffRepository.existsById(1L)).thenReturn(true);
        when(caseloadRepository.findByStaffStaffIdAndRoleCodeIn(1L, asList("OM", "OS")))
            .thenReturn(asList(
                Caseload.builder().roleCode("OM").team(aTeam().toBuilder().teamId(2L).build()).staff(aStaff()).build(),
                Caseload.builder().roleCode("OS").team(aTeam().toBuilder().teamId(3L).build()).staff(aStaff()).build()
            ));

        var caseload = caseloadService.getCaseloadByStaffIdentifier(1L, OFFENDER_MANAGER, ORDER_SUPERVISOR);

        assertThat(caseload).isPresent();
        assertThat(caseload.get().getManagedOffenders())
            .hasSize(1)
            .element(0).extracting(ManagedOffenderCrn::getTeamIdentifier).isEqualTo(2L);
        assertThat(caseload.get().getSupervisedOrders())
            .hasSize(1)
            .element(0).extracting(ManagedEventId::getTeamIdentifier).isEqualTo(3L);
    }

    @Test
    public void whenTeamCaseloadIsReturnedFromRepository_thenMapAndReturnIt() {
        final Team team = new Team();
        team.setTeamId(37L);

        when(teamRepository.findByCode("TEST")).thenReturn(Optional.of(team));
        when(caseloadRepository.findByTeamTeamIdAndRoleCodeIn(eq(team.getTeamId()), eq(asList("OM", "OS")), any(Pageable.class)))
            .thenReturn(asList(
                Caseload.builder().roleCode("OM").team(aTeam()).staff(aStaff().toBuilder().staffId(2L).build()).build(),
                Caseload.builder().roleCode("OS").team(aTeam()).staff(aStaff().toBuilder().staffId(3L).build()).build()
            ));

        var caseload =
            caseloadService.getCaseloadByTeamCode("TEST", PageRequest.of(0, 100), OFFENDER_MANAGER, ORDER_SUPERVISOR);

        assertThat(caseload).isPresent();
        assertThat(caseload.get().getManagedOffenders())
            .hasSize(1)
            .element(0).extracting(ManagedOffenderCrn::getStaffIdentifier).isEqualTo(2L);
        assertThat(caseload.get().getSupervisedOrders())
            .hasSize(1)
            .element(0).extracting(ManagedEventId::getStaffIdentifier).isEqualTo(3L);
    }

}
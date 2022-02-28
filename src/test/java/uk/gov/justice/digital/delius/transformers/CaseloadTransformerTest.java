package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.ManagedEventId;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;
import uk.gov.justice.digital.delius.jpa.standard.entity.Caseload;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;

public class CaseloadTransformerTest {

    @Test
    public void managedOffenderCrnDetailsAreCorrect() {
        Caseload source = Caseload.builder()
            .caseloadId(new Random().nextLong())
            .crn(UUID.randomUUID().toString())
            .roleCode("OM")
            .allocationDate(LocalDate.now())
            .staff(aStaff(UUID.randomUUID().toString()))
            .team(aTeam(UUID.randomUUID().toString()))
            .build();

        assertThat(CaseloadTransformer.managedOffenderCrnOf(source))
            .isEqualTo(ManagedOffenderCrn.builder()
                .offenderCrn(source.getCrn())
                .allocationDate(LocalDate.now())
                .staffIdentifier(source.getStaff().getStaffId())
                .staff(StaffTransformer.staffOf(source.getStaff()))
                .teamIdentifier(source.getTeam().getTeamId())
                .team(TeamTransformer.teamOf(source.getTeam()))
                .build());
    }

    @Test
    public void managedEventIdDetailsAreCorrect() {
        Random random = new Random();
        Caseload source = Caseload.builder()
            .caseloadId(random.nextLong())
            .crn(UUID.randomUUID().toString())
            .eventId(random.nextLong())
            .roleCode("OS")
            .allocationDate(LocalDate.now())
            .staff(aStaff(UUID.randomUUID().toString()))
            .team(aTeam(UUID.randomUUID().toString()))
            .build();

        assertThat(CaseloadTransformer.managedEventIdOf(source))
            .isEqualTo(ManagedEventId.builder()
                .offenderCrn(source.getCrn())
                .eventId(source.getEventId())
                .allocationDate(LocalDate.now())
                .staffIdentifier(source.getStaff().getStaffId())
                .staff(StaffTransformer.staffOf(source.getStaff()))
                .teamIdentifier(source.getTeam().getTeamId())
                .team(TeamTransformer.teamOf(source.getTeam()))
                .build());
    }

    @Test
    public void caseloadDetailsAreCorrect() {
        Random random = new Random();
        Caseload offenderManager = Caseload.builder()
            .caseloadId(new Random().nextLong())
            .crn(UUID.randomUUID().toString())
            .roleCode("OM")
            .allocationDate(LocalDate.now())
            .allocationDate(LocalDate.now())
            .staff(aStaff(UUID.randomUUID().toString()))
            .team(aTeam(UUID.randomUUID().toString()))
            .build();
        Caseload orderSupervisor = Caseload.builder()
            .caseloadId(random.nextLong())
            .crn(UUID.randomUUID().toString())
            .eventId(random.nextLong())
            .roleCode("OS")
            .allocationDate(LocalDate.now())
            .staff(aStaff(UUID.randomUUID().toString()))
            .team(aTeam(UUID.randomUUID().toString()))
            .build();

        assertThat(CaseloadTransformer.caseloadOf(Arrays.asList(offenderManager, orderSupervisor)))
            .usingRecursiveComparison()
            .isEqualTo(uk.gov.justice.digital.delius.data.api.Caseload.builder()
                .managedOffenders(Set.of(CaseloadTransformer.managedOffenderCrnOf(offenderManager)))
                .supervisedOrders(Set.of(CaseloadTransformer.managedEventIdOf(orderSupervisor)))
                .build());
    }

}

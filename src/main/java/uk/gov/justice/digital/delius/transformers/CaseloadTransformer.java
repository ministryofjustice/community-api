package uk.gov.justice.digital.delius.transformers;

import lombok.val;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.ManagedEventId;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static uk.gov.justice.digital.delius.data.api.CaseloadRole.OFFENDER_MANAGER;
import static uk.gov.justice.digital.delius.data.api.CaseloadRole.ORDER_SUPERVISOR;

public class CaseloadTransformer {
    public static Caseload caseloadOf(final List<uk.gov.justice.digital.delius.jpa.standard.entity.Caseload> caseload) {
        val groupedCaseload = caseload.stream().collect(groupingBy(entity -> entity.getRoleCode()));
        return Caseload.builder()
            .managedOffenders(Optional.ofNullable(groupedCaseload.get(OFFENDER_MANAGER.getRoleCode()))
                .map(managedOffenders -> managedOffenders.stream()
                    .map(CaseloadTransformer::managedOffenderCrnOf)
                    .collect(toSet()))
                .orElse(emptySet()))
            .supervisedOrders(Optional.ofNullable(groupedCaseload.get(ORDER_SUPERVISOR.getRoleCode()))
                .map(supervisedOrders -> supervisedOrders.stream()
                    .map(CaseloadTransformer::managedEventIdOf)
                    .collect(toSet()))
                .orElse(emptySet()))
            .build();
    }

    public static ManagedOffenderCrn managedOffenderCrnOf(final uk.gov.justice.digital.delius.jpa.standard.entity.Caseload caseload) {
        return ManagedOffenderCrn.builder()
            .offenderCrn(caseload.getCrn())
            .allocationDate(caseload.getAllocationDate())
            .staff(StaffTransformer.staffOf(caseload.getStaff()))
            .staffIdentifier(caseload.getStaff().getStaffId())
            .team(TeamTransformer.teamOf(caseload.getTeam()))
            .teamIdentifier(caseload.getTeam().getTeamId())
            .build();
    }

    public static ManagedEventId managedEventIdOf(final uk.gov.justice.digital.delius.jpa.standard.entity.Caseload caseload) {
        return ManagedEventId.builder()
            .eventId(caseload.getEventId())
            .allocationDate(caseload.getAllocationDate())
            .staff(StaffTransformer.staffOf(caseload.getStaff()))
            .staffIdentifier(caseload.getStaff().getStaffId())
            .team(TeamTransformer.teamOf(caseload.getTeam()))
            .teamIdentifier(caseload.getTeam().getTeamId())
            .build();
    }
}

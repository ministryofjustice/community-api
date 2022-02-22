package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagedEventId {
    private Long eventId;

    private LocalDate allocationDate;

    private Long staffIdentifier;
    private StaffHuman staff;

    private Long teamIdentifier;
    private Team team;
}

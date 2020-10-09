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
public class OffenderManager {
    private Human trustOfficer;
    private StaffHuman staff;
    private Human providerEmployee;
    private String partitionArea;
    private Boolean softDeleted;
    private Team team;
    private ProbationArea probationArea;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Boolean active;
    private KeyValue allocationReason;

}

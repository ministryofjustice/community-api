package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class OffenderManager {
    private Human trustOfficer;
    private Human staff;
    private Human providerEmployee;
    private String partitionArea;
    private Boolean softDeleted;
    private Team team;
    private KeyValue probationArea;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Boolean active;

}

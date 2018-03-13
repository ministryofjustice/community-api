package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OffenderManager {
    private Human trustOfficer;
    private String partitionArea;
    private Boolean softDeleted;
    private Team team;
    private KeyValue probationArea;

}

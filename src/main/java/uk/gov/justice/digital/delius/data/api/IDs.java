package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IDs {
    @ApiModelProperty(required = true)
    private String crn;
    private String pncNumber;
    private String croNumber;
    private String niNumber;
    private String nomsNumber;
    private String immigrationNumber;
    private String mostRecentPrisonerNumber;
}

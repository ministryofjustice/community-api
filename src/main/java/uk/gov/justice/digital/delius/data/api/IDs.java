package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

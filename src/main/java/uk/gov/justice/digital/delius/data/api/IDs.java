package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class IDs {
    @ApiModelProperty(required = true)
    private String crn;
    private Optional<String> pncNumber;
    private Optional<String> croNumber;
    private Optional<String> niNumber;
    private Optional<String> nomsNumber;
    private Optional<String> immigrationNumber;
    private Optional<String> mostRecentPrisonerNumber;
}

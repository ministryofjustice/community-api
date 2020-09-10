package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PssRequirement {
    @ApiModelProperty(value = "Unique identifier for the pssRequirement", required = true)
    private Long pssRequirementId;
    private KeyValue type;
    private KeyValue subType;
    @ApiModelProperty(value = "Is the requirement currently active")
    private Boolean active;
}

package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PssRequirement {
    private KeyValue type;
    private KeyValue subType;
    @ApiModelProperty(value = "Is the requirement currently active")
    private Boolean active;
}

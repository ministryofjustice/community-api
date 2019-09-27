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
public class AccessLimitation {
    @ApiModelProperty(required = true)
    private boolean userRestricted;
    private String restrictionMessage;
    @ApiModelProperty(required = true)
    private boolean userExcluded;
    private String exclusionMessage;
}

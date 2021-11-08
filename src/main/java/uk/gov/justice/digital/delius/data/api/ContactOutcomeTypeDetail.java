package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactOutcomeTypeDetail {
    @ApiModelProperty(required = true)
    private String code;

    @ApiModelProperty(required = true)
    private String description;

    @ApiModelProperty(name = "Is this outcome compliant/acceptable")
    private Boolean compliantAcceptable;

    @ApiModelProperty(name = "Does this outcome indicate attendance")
    private Boolean attendance;

    @ApiModelProperty(name = "Is an enforcement action mandatory", required = true)
    private Boolean actionRequired;

    @ApiModelProperty(name = "Can an enforcement action can be supplied")
    private Boolean enforceable;

    @ApiModelProperty(name = "Available enforcement actions", required = true)
    private List<EnforcementAction> enforcements;
}

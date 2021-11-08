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
public class EnforcementAction {
    @ApiModelProperty(required = true)
    private String code;

    @ApiModelProperty(required = true)
    private String description;

    @ApiModelProperty("Contact will be added to the enforcement diary")
    private Boolean outstandingContactAction;

    @ApiModelProperty("Enforcement response date on the contact will be populated as this many days from the outcome date")
    private Long responseByPeriod;
}

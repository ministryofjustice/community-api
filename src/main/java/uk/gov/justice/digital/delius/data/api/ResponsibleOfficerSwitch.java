package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

import jakarta.validation.constraints.AssertTrue;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ApiModel(description = "Request body for switching the responsible officer")
public class ResponsibleOfficerSwitch {
    @ApiModelProperty(value = "true if the RO should be set the the current community offender manager", example = "true")
    private boolean switchToCommunityOffenderManager;
    @ApiModelProperty(value = "true if the RO should be set the the current prison offender manager", example = "false")
    private boolean switchToPrisonOffenderManager;

    @SuppressWarnings("unused")
    @AssertTrue(message="Either set true for the prisoner offender manager or the community offender manager")
    @ApiIgnore
    private boolean isValid() {
        return switchToCommunityOffenderManager != switchToPrisonOffenderManager;
    }
}

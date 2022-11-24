package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextlessAppointmentOutcomeRequest {

    @NotNull
    @ApiModelProperty(required = true)
    private String notes;

    @NotNull
    @ApiModelProperty(required = true)
    private String attended;

    @NotNull
    @ApiModelProperty(required = true)
    private Boolean notifyPPOfAttendanceBehaviour;
}
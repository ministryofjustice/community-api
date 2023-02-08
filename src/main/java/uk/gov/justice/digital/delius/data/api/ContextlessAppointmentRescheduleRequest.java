package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextlessAppointmentRescheduleRequest {

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime updatedAppointmentStart;

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime updatedAppointmentEnd;

    @NotNull
    @ApiModelProperty(required = true)
    private Boolean initiatedByServiceProvider;

    @ApiModelProperty
    private String officeLocationCode;
}
package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime appointmentStart;

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime appointmentEnd;

    @NotNull
    @ApiModelProperty(required = true)
    private String officeLocationCode;

    @NotNull
    @ApiModelProperty(required = true)
    private String notes;

    @NotNull
    @ApiModelProperty(required = true)
    private String context;
}
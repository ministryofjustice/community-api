package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {

    @NotNull
    @ApiModelProperty(required = true)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate appointmentDate;

    @NotNull
    @ApiModelProperty(required = true)
    @JsonFormat(pattern="HH:mm:ss")
    private LocalTime appointmentStartTime;

    @NotNull
    @ApiModelProperty(required = true)
    @JsonFormat(pattern="HH:mm:ss")
    private LocalTime appointmentEndTime;

    @NotNull
    @ApiModelProperty(required = true)
    private String officeLocationCode;

    @NotNull
    @ApiModelProperty(required = true)
    private Origin origin;
}
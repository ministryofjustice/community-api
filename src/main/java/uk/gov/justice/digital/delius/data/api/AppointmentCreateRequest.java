package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {
    private Long eventId;
    @ApiModelProperty(required = true)
    private String appointmentType;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate appointmentDate;
    @JsonFormat(pattern="HH:mm:ss")
    private LocalTime appointmentStartTime;
    @JsonFormat(pattern="HH:mm:ss")
    private LocalTime appointmentEndTime;
    private String staffCode;
    private String teamCode;
    private String officeLocationCode;
    private String probationAreaCode;
}

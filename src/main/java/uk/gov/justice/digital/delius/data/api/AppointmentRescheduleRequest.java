package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRescheduleRequest {

    @NotNull
    @Schema(required = true)
    private OffsetDateTime updatedAppointmentStart;

    @NotNull
    @Schema(required = true)
    private OffsetDateTime updatedAppointmentEnd;

    @NotNull
    @Schema(required = true)
    private String outcome;

    @Schema
    private String officeLocationCode;
}
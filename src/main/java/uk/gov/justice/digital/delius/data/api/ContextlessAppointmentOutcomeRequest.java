package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextlessAppointmentOutcomeRequest {

    @NotNull
    @Schema(required = true)
    private String notes;

    @NotNull
    @Schema(required = true)
    private String attended;

    @NotNull
    @Schema(required = true)
    private Boolean notifyPPOfAttendanceBehaviour;
}
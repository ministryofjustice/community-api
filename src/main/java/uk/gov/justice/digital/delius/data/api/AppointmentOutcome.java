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
public class AppointmentOutcome {
    @NotNull
    @Schema(name = "Code", example = "ABC123")
    private String code;

    @NotNull
    @Schema(name = "Description", example = "Some appointment outcome")
    private String description;

    @Schema(name = "Attended", example = "true")
    private Boolean attended;

    @Schema(name = "Complied", example = "true")
    private Boolean complied;

    @Schema(name = "Hours credited", example = "1.5")
    private Double hoursCredited;
}

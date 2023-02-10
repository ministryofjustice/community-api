package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOffenderNomsNumber {
    @Schema(description = "NOMS number to be set on the offender. AKA offenderNo", example = "G5555TT")
    @NotBlank(message = "Missing a NOMS number")
    private String nomsNumber;
}

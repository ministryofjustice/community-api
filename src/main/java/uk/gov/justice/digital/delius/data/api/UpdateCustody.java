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
public class UpdateCustody {
    @Schema(description = "Prison institution code in NOMIS", example = "MDI")
    @NotBlank(message = "Missing a NOMS prison institution code in nomsPrisonInstitutionCode")
    private String nomsPrisonInstitutionCode;
}

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
public class UpdateOffenderDetails {
    @NotBlank(message = "Missing firstname")
    @Schema(example = "John")
    private String firstName;
    @NotBlank(message = "Missing surname")
    @Schema(example = "Smith")
    private String surname;
}

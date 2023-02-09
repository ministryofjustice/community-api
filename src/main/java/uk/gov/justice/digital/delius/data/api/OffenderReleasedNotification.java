package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OffenderReleasedNotification {
    @NotBlank(message = "Missing a NOMS prison institution code in nomsPrisonInstitutionCode")
    @Schema(description = "The Prison institution code in NOMIS the offender was released from", required = true, example = "MDI")
    private String nomsPrisonInstitutionCode;

    @NotBlank(message = "Missing a NOMS release reason code")
    @Schema(description = "The release reason code in NOMIS", required = true, example = "RELEASE")
    private String reason;

    @Schema(description = "The date the offender was released from custody", example = "2020-10-25")
    private LocalDate releaseDate;
}

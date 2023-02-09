package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OffenderRecalledNotification {
    @NotBlank(message = "Missing a NOMS prison institution code in nomsPrisonInstitutionCode")
    @Schema(description = "The Prison institution code in NOMIS the offender was recalled to", required = true, example = "MDI")
    private String nomsPrisonInstitutionCode;

    @NotNull(message = "Missing the date the offender was returned to custody in recallDate")
    @Schema(description = "The date the offender was returned to custody", required = true, example = "2020-10-25")
    private LocalDate recallDate;

    @Schema(
        description = "The NOMIS reason for the admission. Only ADMISSION and TEMPORARY_ABSENCE_RETURN are accepted as reasons for recall.",
        example = "ADMISSION",
        allowableValues = "ADMISSION, TEMPORARY_ABSENCE_RETURN, RETURN_FROM_COURT, TRANSFERRED, UNKNOWN")
    private String reason;

    @Schema(
        description = "Probable cause for the admission. Currently present only for admissions. It has been observed that administration mistakes in NOMIS can cause the reason for RECALL and CONVICTED to be not always be accurate.",
        example = "RECALL",
        allowableValues = "RECALL, REMAND, CONVICTED, IMMIGRATION_DETAINEE, UNKNOWN")
    private String probableCause;
}

package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OffenderRecalledNotification {
    @NotBlank(message = "Missing a NOMS prison institution code in nomsPrisonInstitutionCode")
    @ApiModelProperty(value = "The Prison institution code in NOMIS the offender was recalled to", required = true, example = "MDI")
    private String nomsPrisonInstitutionCode;

    @NotNull(message = "Missing the date the offender was returned to custody in recallDate")
    @ApiModelProperty(value = "The date the offender was returned to custody", required = true, example = "2020-10-25")
    private LocalDate recallDate;

    @ApiModelProperty(value = "The NOMIS reason for the admission.", example = "ADMISSION",
        allowableValues = "ADMISSION, TEMPORARY_ABSENCE_RETURN, RETURN_FROM_COURT, TRANSFERRED, UNKNOWN",
        notes = "Only ADMISSION and TEMPORARY_ABSENCE_RETURN are accepted as reasons for recall.")
    private String reason;

    @ApiModelProperty(value = "Probable cause for the admission.", example = "RECALL",
        allowableValues = "RECALL, REMAND, CONVICTED, IMMIGRATION_DETAINEE, UNKNOWN",
        notes = "Currently present only for admissions. It has been observed that administration mistakes in NOMIS can cause the reason for RECALL and CONVICTED to be not always be accurate.")
    private String probableCause;
}

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderRecalledNotification {
    @NotBlank(message = "Missing a NOMS prison institution code in nomsPrisonInstitutionCode")
    @ApiModelProperty(value = "The Prison institution code in NOMIS the offender was recalled to", required = true, example = "MDI")
    private String nomsPrisonInstitutionCode;

    @NotNull(message = "Missing the date the offender was returned to custody in recallDate")
    @ApiModelProperty(value = "The date the offender was returned to custody", required = true, example = "2020-10-25")
    private LocalDate recallDate;
}

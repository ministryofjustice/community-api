package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustody {
    @ApiModelProperty(value = "Prison institution code in NOMIS", example = "MDI")
    @NotBlank(message = "Missing a NOMS prison institution code in nomsPrisonInstitutionCode")
    private String nomsPrisonInstitutionCode;
}

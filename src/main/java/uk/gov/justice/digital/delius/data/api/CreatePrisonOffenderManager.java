package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrisonOffenderManager {
    @ApiModelProperty(value = "Name of offender manager")
    private Human officer;
    @ApiModelProperty(value = "Optional officer staff code, if not present name will be used to lookup staff member", required = false)
    private String officerCode;
    @ApiModelProperty(value = "Prison institution code in NOMIS")
    private String nomsPrisonInstitutionCode;
}

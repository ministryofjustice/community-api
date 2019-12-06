package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "Request body for assigning an offender manager to an offender. Must pass exactly one of office / officerCode (not both)")
public class CreatePrisonOffenderManager {
    @ApiModelProperty(value = "Name of offender manager. If passed then must contain both forename(s) and surname", example = "officer: {\"forenames\": \"John\", \"surname\": \"Smith\" }")
    private Human officer;
    @ApiModelProperty(value = "Officer staff code. If not present name will be used to lookup staff member", example = "N07A001")
    private String officerCode;
    @ApiModelProperty(value = "Prison institution code in NOMIS", required = true, example = "MDI")
    private String nomsPrisonInstitutionCode;
}

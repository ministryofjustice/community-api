package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Court details for a new court")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewCourtDto {
    @ApiModelProperty(value = "unique code for this court", example = "SALEMC")
    @NotBlank(message = "Court code is required")
    private String code;
    @ApiModelProperty(value = "type code from standard reference data", example = "MAG")
    @NotBlank(message = "Court type code is required")
    private String courtTypeCode;
    @ApiModelProperty(value = "true when this court is open")
    private boolean active;
    @NotBlank(message = "Court name is required")
    private String courtName;
    private String telephoneNumber;
    private String fax;
    private String buildingName;
    private String street;
    private String locality;
    private String town;
    @ApiModelProperty(example = "South Yorkshire")
    private String county;
    private String postcode;
    @ApiModelProperty(example = "England")
    private String country;
    @ApiModelProperty(value = "probation area code from probation areas", example = "N51")
    @NotBlank(message = "Probation area code is required")
    private String probationAreaCode;
}

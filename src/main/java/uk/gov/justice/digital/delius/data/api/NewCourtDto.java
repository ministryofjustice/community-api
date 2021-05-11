package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Court details for a new court")
public record NewCourtDto(
    @ApiModelProperty(value = "unique code for this court", example = "SALEMC")
    @NotBlank(message = "Court code is required")
    String code,
    @ApiModelProperty(value = "type code from standard reference data", example = "MAG")
    @NotBlank(message = "Court type code is required")
    String courtTypeCode,
    @ApiModelProperty(value = "true when this court is open")
    boolean active,
    @NotBlank(message = "Court name is required")
    String courtName,
    String telephoneNumber,
    String fax,
    String buildingName,
    String street,
    String locality,
    String town,
    @ApiModelProperty(example = "South Yorkshire")
    String county,
    String postcode,
    @ApiModelProperty(example = "England")
    String country,
    @ApiModelProperty(value = "probation area code from probation areas", example = "N51")
    @NotBlank(message = "Probation area code is required")
    String probationAreaCode
) {
}

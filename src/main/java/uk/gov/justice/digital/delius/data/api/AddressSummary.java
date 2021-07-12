package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AddressSummary {
    @ApiModelProperty(name = "Building number", example = "32")
    private String addressNumber;

    @ApiModelProperty(name = "Building name", example = "HMPPS Digital Studio")
    private String buildingName;

    @ApiModelProperty(name = "Street name", example = "Scotland Street")
    private String streetName;

    @ApiModelProperty(name = "District", example = "Sheffield City Centre")
    private String district;

    @ApiModelProperty(name = "Town or city", example = "Sheffield")
    private String town;

    @ApiModelProperty(name = "County", example = "South Yorkshire")
    private String county;

    @ApiModelProperty(name = "Postcode", example = "S3 7BS")
    private String postcode;

    @ApiModelProperty(name = "Telephone number", example = "0123456789")
    private String telephoneNumber;
}

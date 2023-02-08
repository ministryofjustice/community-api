package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeLocation {
    @NotNull
    @ApiModelProperty(name = "Office location code", example = "ASP_ASH", position = 1)
    private String code;

    @NotNull
    @ApiModelProperty(name = "Description", example = "Ashley House Approved Premises", position = 2)
    private String description;

    @ApiModelProperty(name = "Building name", example = "Ashley House", position = 3)
    private String buildingName;

    @ApiModelProperty(name = "Building number", example = "14", position = 4)
    private String buildingNumber;

    @ApiModelProperty(name = "Street name", example = "Somerset Street", position = 5)
    private String streetName;

    @ApiModelProperty(name = "Town or city", example = "Bristol", position = 6)
    private String townCity;

    @ApiModelProperty(name = "County", example = "Somerset", position = 7)
    private String county;

    @ApiModelProperty(name = "Postcode", example = "BS2 8NB", position = 8)
    private String postcode;
}

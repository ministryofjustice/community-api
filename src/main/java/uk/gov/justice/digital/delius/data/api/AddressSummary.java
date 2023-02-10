package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AddressSummary {
    @Schema(name = "Building number", example = "32")
    private String addressNumber;

    @Schema(name = "Building name", example = "HMPPS Digital Studio")
    private String buildingName;

    @Schema(name = "Street name", example = "Scotland Street")
    private String streetName;

    @Schema(name = "District", example = "Sheffield City Centre")
    private String district;

    @Schema(name = "Town or city", example = "Sheffield")
    private String town;

    @Schema(name = "County", example = "South Yorkshire")
    private String county;

    @Schema(name = "Postcode", example = "S3 7BS")
    private String postcode;

    @Schema(name = "Telephone number", example = "0123456789")
    private String telephoneNumber;
}

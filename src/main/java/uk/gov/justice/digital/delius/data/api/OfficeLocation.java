package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeLocation {
    @NotNull
    @Schema(name = "Office location code", example = "ASP_ASH")
    private String code;

    @NotNull
    @Schema(name = "Description", example = "Ashley House Approved Premises")
    private String description;

    @Schema(name = "Building name", example = "Ashley House")
    private String buildingName;

    @Schema(name = "Building number", example = "14")
    private String buildingNumber;

    @Schema(name = "Street name", example = "Somerset Street")
    private String streetName;

    @Schema(name = "Town or city", example = "Bristol")
    private String townCity;

    @Schema(name = "County", example = "Somerset")
    private String county;

    @Schema(name = "Postcode", example = "BS2 8NB")
    private String postcode;
}

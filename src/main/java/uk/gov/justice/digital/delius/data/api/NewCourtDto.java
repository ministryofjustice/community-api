package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Court details for a new court")
public record NewCourtDto(
    @Schema(description = "unique code for this court", example = "SALEMC")
    @NotBlank(message = "Court code is required")
    String code,
    @Schema(description = "type code from standard reference data", example = "MAG")
    @NotBlank(message = "Court type code is required")
    String courtTypeCode,
    @Schema(description = "true when this court is open")
    boolean active,
    @NotBlank(message = "Court name is required")
    String courtName,
    String telephoneNumber,
    String fax,
    String buildingName,
    String street,
    String locality,
    String town,
    @Schema(example = "South Yorkshire")
    String county,
    String postcode,
    @Schema(example = "England")
    String country,
    @Schema(description = "probation area code from probation areas", example = "N51")
    @NotBlank(message = "Probation area code is required")
    String probationAreaCode
) {
}

package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Court details for updating an exiting court")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourtDto {
    @Schema(description = "type code from standard reference data", example = "MAG")
    @NotBlank(message = "Court type code is required")
    private String courtTypeCode;
    @Schema(description = "true when this court is open")
    private boolean active;
    @NotBlank(message = "Court name is required")
    private String courtName;
    private String telephoneNumber;
    private String fax;
    private String buildingName;
    private String street;
    private String locality;
    private String town;
    @Schema(example = "South Yorkshire")
    private String county;
    private String postcode;
    @Schema(example = "England")
    private String country;
}

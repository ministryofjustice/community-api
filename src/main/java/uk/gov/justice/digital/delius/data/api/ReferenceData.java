package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceData {
    @Schema(description = "true if this item is currently selectable in Delius")
    private boolean active;
    @Schema(description = "code of reference data", example = "VISO")
    private String code;
    @Schema(description = "description of reference data", example = "ViSOR Number")
    private String description;
}

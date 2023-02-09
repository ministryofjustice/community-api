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
public class LocalDeliveryUnit {
    @Schema(required = true)
    private Long localDeliveryUnitId;
    @Schema(description = "LDU code", example = "N01KSCT")
    private String code;
    @Schema(description = "description", example = "NPS Manchester City South")
    private String description;
}

package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProbationAreaWithLocalDeliveryUnits {
    @Schema(description = "area code", example = "N01")
    private String code;
    @Schema(description = "description", example = "NPS North West")
    private String description;
    private List<LocalDeliveryUnit> localDeliveryUnits;
}

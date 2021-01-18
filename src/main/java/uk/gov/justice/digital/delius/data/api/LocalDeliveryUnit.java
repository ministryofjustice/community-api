package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalDeliveryUnit {
    @ApiModelProperty(required = true)
    private Long localDeliveryUnitId;
    @ApiModelProperty(value = "LDU code", example = "N01KSCT")
    private String code;
    @ApiModelProperty(value = "description", example = "NPS Manchester City South")
    private String description;
}

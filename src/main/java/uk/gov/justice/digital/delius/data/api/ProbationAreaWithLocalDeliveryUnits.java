package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "area code", example = "N01")
    private String code;
    @ApiModelProperty(value = "description", example = "NPS North West")
    private String description;
    private List<LocalDeliveryUnit> localDeliveryUnits;
}

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
public class ReferenceData {
    @ApiModelProperty(value = "true if this item is currently selectable in Delius")
    private boolean active;
    @ApiModelProperty(value = "code of reference data", example = "VISO")
    private String code;
    @ApiModelProperty(value = "description of reference data", example = "ViSOR Number")
    private String description;
}

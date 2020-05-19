package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "Reference data list")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceDataList {
    @ApiModelProperty(value = "List of reference data items")
    private List<ReferenceData> referenceData;
}

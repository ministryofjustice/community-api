package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "Reference data sets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceDataSets {
    @ApiModelProperty(value = "List of reference data sets, for example \n{\n" +
            "            \"code\": \"ADDITIONAL SENTENCE\",\n" +
            "            \"description\": \"Additional Sentence\"\n" +
            "        }")
    private List<KeyValue> referenceDataSets;
}

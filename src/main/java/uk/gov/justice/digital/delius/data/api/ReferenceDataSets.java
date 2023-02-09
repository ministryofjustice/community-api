package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Reference data sets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceDataSets {
    @Schema(description = "List of reference data sets, for example \n{\n" +
            "            \"code\": \"ADDITIONAL SENTENCE\",\n" +
            "            \"description\": \"Additional Sentence\"\n" +
            "        }")
    private List<KeyValue> referenceDataSets;
}

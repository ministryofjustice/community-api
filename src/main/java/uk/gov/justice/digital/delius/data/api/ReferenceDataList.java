package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Reference data list")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceDataList {
    @Schema(description = "List of reference data items")
    private List<ReferenceData> referenceData;
}

package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Offender Identifiers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderIdentifiers {
    @Schema(description = "unique identifier for this offender", example = "1234567")
    private Long offenderId;
    @Schema(description = "Primary identifiers")
    private IDs primaryIdentifiers;
    @Schema(description = "Additional identifiers")
    private List<AdditionalIdentifier> additionalIdentifiers;
}

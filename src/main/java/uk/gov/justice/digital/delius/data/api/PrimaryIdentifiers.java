package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Offender primary identifiers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryIdentifiers {
    @Schema(description = "unique identifier for this offender", example = "1234567")
    private Long offenderId;
    @Schema(description = "case reference number", required = true, example = "12345C")
    private String crn;
}

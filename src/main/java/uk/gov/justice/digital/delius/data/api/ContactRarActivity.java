package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactRarActivity {
    @NotNull
    @Schema(name = "The ID of the RAR requirement")
    private Long requirementId;

    @Schema(name = "The ID of the RAR NSI if present")
    private Long nsiId;

    @Schema(name = "The RAR type if known")
    private KeyValue type;

    @Schema(name = "The RAR subtype if known")
    private KeyValue subtype;
}

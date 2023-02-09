package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyValue {
    @Schema(name = "Code", example = "ABC123")
    private String code;

    @Schema(name = "Description", example = "Some description")
    private String description;
}

package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OffenderRecall {
    @Schema(description = "The date the recall occurred", example = "2019-11-27")
    private LocalDate date;
    @Schema(description = "The reason for the recall")
    private KeyValue reason;
    @Schema(description = "Some notes")
    private String notes;
}

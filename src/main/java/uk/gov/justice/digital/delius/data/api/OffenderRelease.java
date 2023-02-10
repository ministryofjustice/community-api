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
public class OffenderRelease {
    @Schema(description = "The date the release occurred", example = "2019-11-26")
    private LocalDate date;
    @Schema(description = "Some notes")
    private String notes;
    @Schema(description = "The institution the offender was released from")
    private Institution institution;
    @Schema(description = "The reason for the release")
    private KeyValue reason;
}

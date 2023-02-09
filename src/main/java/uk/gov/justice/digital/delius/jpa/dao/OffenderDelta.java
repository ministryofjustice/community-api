package uk.gov.justice.digital.delius.jpa.dao;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"offenderId", "dateChanged", "action"})
public class OffenderDelta {
    @Schema(description = "Offender ID", example = "232423")
    private Long offenderId;
    @Schema(description = "The datetime the change occurred", example = "2019-11-27T15:12:43.000Z")
    private LocalDateTime dateChanged;
    @Schema(description = "Type of delta", example = "UPSERT", allowableValues = "UPSERT,DELETE")
    private String action;
}

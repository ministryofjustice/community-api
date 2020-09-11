package uk.gov.justice.digital.delius.jpa.dao;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "Offender ID", example = "232423", position = 1)
    private Long offenderId;
    @ApiModelProperty(value = "The datetime the change occurred", example = "2019-11-27T15:12:43.000Z", position = 2)
    private LocalDateTime dateChanged;
    @ApiModelProperty(value = "Type of delta", example = "UPSERT", allowableValues = "UPSERT,DELETE", position = 3)
    private String action;
}

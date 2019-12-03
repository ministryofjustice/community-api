package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "The date the recall occurred", example = "2019-11-27")
    private LocalDate date;
    @ApiModelProperty(value = "The reason for the recall")
    private KeyValue reason;
    @ApiModelProperty(value = "Some notes")
    private String notes;
}

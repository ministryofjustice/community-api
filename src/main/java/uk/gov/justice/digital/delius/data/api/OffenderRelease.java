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
public class OffenderRelease {
    @ApiModelProperty(value = "The date the release occurred", example = "2019-11-26")
    private LocalDate date;
    @ApiModelProperty(value = "Some notes")
    private String notes;
    @ApiModelProperty(value = "The institution the offender was released from")
    private Institution institution;
}

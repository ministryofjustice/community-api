package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderReleasedNotification {
    @ApiModelProperty(value = "The date the offender was released from custody", example = "2020-10-25")
    private LocalDate occurrred;
}

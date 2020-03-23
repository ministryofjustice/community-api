package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Appointments {
    @ApiModelProperty(value = "Total number of appointments to date")
    private Long total;
    @ApiModelProperty(value = "Number of appointments recorded to date as attended", position = 1)
    private Long attended;
    @ApiModelProperty(value = "Number of appointments recorded to date as not attended and compliant - i.e. with an acceptable reason for absence", position = 2)
    private Long acceptableAbsences;
    @ApiModelProperty(value = "Number of appointments recorded to date as not attended and uncompliant - i.e. without an acceptable reason for absence", position = 3)
    private Long unacceptableAbsences;
    @ApiModelProperty(value = "Number of appointments to date where no attendance or compliance information has been recorded", position = 4)
    private Long noOutcomeRecorded;
}

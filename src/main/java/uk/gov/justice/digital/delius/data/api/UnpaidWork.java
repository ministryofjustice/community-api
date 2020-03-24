package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnpaidWork {
    @ApiModelProperty(value = "Minutes of unpaid work ordered for this sentence")
    private Long minutesOrdered;
    @ApiModelProperty(value = "Minutes of unpaid work credited to the service user to date")
    private Long minutesCompleted;
    @ApiModelProperty(value = "Details of appointment history to date")
    private Appointments appointments;
}

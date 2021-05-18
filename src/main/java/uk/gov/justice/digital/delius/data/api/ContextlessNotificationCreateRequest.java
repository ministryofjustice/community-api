package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextlessNotificationCreateRequest {

    // Fields used to identify associated Referral/Nsi
    @NotNull
    @ApiModelProperty(required = true)
    private String contractType;

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime referralStart;

    // Fields used for creating the appointment
    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime contactDateTime;

    @NotNull
    @ApiModelProperty(required = true)
    private String notes;
}
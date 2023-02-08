package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

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

    @ApiModelProperty
    private UUID referralId;

    // Fields used for creating the contact
    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime contactDateTime;

    @NotNull
    @ApiModelProperty(required = true)
    private String notes;
}
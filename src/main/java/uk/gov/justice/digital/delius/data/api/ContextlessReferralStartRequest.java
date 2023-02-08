package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@With
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ContextlessReferralStartRequest {

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime startedAt;

    @NotNull
    @ApiModelProperty(required = true, value = "Denotes a group of services delivered through a referral to a service user, e.g. Personal Well Being", example = "PWB")
    private String contractType;

    @Positive
    @NotNull
    @ApiModelProperty(required = true)
    private Long sentenceId;

    @ApiModelProperty
    private UUID referralId;

    @NotNull
    private String notes;
}

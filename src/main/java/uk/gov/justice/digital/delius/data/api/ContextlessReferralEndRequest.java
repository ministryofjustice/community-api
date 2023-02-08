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
public class ContextlessReferralEndRequest {

    // Fields used to identify associated Referral/Nsi
    @NotNull
    @ApiModelProperty(required = true, value = "Denotes a group of services delivered through a referral to a service user, e.g. Personal Well Being", example = "PWB")
    private String contractType;

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime startedAt;

    @ApiModelProperty
    private UUID referralId;

    // Fields used for ending the referral
    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime endedAt;

    @Positive
    @NotNull
    @ApiModelProperty(required = true)
    private Long sentenceId;

    @NotNull
    @ApiModelProperty(required = true)
    private String endType;

    @NotNull
    private String notes;
}

package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@With
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReferralSentRequest {

    @NotNull
    @ApiModelProperty(required = true)
    private OffsetDateTime sentAt;

    @NotNull
    @ApiModelProperty(required = true)
    private UUID serviceCategoryId;

    @Positive
    @NotNull
    @ApiModelProperty(required = true)
    private Long sentenceId;

    @NotNull
    private String notes;

    @NotNull
    private String context;
}

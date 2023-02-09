package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@With
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ContextlessReferralStartRequest {

    @NotNull
    @Schema(required = true)
    private OffsetDateTime startedAt;

    @NotNull
    @Schema(required = true, description = "Denotes a group of services delivered through a referral to a service user, e.g. Personal Well Being", example = "PWB")
    private String contractType;

    @Positive
    @NotNull
    @Schema(required = true)
    private Long sentenceId;

    @Schema
    private UUID referralId;

    @NotNull
    private String notes;
}

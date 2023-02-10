package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextlessNotificationCreateRequest {

    // Fields used to identify associated Referral/Nsi
    @NotNull
    @Schema(required = true)
    private String contractType;

    @NotNull
    @Schema(required = true)
    private OffsetDateTime referralStart;

    @Schema
    private UUID referralId;

    // Fields used for creating the contact
    @NotNull
    @Schema(required = true)
    private OffsetDateTime contactDateTime;

    @NotNull
    @Schema(required = true)
    private String notes;
}
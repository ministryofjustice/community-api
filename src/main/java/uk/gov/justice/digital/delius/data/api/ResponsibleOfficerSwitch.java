package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(description = "Request body for switching the responsible officer")
public class ResponsibleOfficerSwitch {
    @Schema(description = "true if the RO should be set the the current community offender manager", example = "true")
    private boolean switchToCommunityOffenderManager;
    @Schema(description = "true if the RO should be set the the current prison offender manager", example = "false")
    private boolean switchToPrisonOffenderManager;

    @SuppressWarnings("unused")
    @AssertTrue(message="Either set true for the prisoner offender manager or the community offender manager")
    @Hidden
    private boolean isValid() {
        return switchToCommunityOffenderManager != switchToPrisonOffenderManager;
    }
}

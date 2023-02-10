package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactOutcomeTypeDetail {
    @Schema(required = true)
    private String code;

    @Schema(required = true)
    private String description;

    @Schema(name = "Is this outcome compliant/acceptable")
    private Boolean compliantAcceptable;

    @Schema(name = "Does this outcome indicate attendance")
    private Boolean attendance;

    @Schema(name = "Is an enforcement action mandatory", required = true)
    private Boolean actionRequired;

    @Schema(name = "Can an enforcement action can be supplied")
    private Boolean enforceable;

    @Schema(name = "Available enforcement actions", required = true)
    private List<EnforcementAction> enforcements;
}

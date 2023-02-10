package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnforcementAction {
    @Schema(required = true)
    private String code;

    @Schema(required = true)
    private String description;

    @Schema(description = "Contact will be added to the enforcement diary")
    private Boolean outstandingContactAction;

    @Schema(description = "Enforcement response date on the contact will be populated as this many days from the outcome date")
    private Long responseByPeriod;
}

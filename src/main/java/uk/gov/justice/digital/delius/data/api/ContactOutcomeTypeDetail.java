package uk.gov.justice.digital.delius.data.api;

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
    private String code;

    private String description;

    private Boolean compliantAcceptable;

    private Boolean attendance;

    private Boolean actionRequired;

    private Boolean enforceable;

    private List<EnforcementAction> enforcements;
}

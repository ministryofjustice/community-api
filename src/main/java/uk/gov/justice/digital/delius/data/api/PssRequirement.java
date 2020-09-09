package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PssRequirement {
    private Long pssRequirementId;
    private KeyValue type;
    private KeyValue subType;
    private Boolean active;
}

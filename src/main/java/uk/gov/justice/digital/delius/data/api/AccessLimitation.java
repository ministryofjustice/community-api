package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessLimitation {
    private boolean userRestricted;
    private boolean userExcluded;
}

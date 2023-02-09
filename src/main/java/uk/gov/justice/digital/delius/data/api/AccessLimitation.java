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
public class AccessLimitation {
    @Schema(required = true)
    private boolean userRestricted;
    private String restrictionMessage;
    @Schema(required = true)
    private boolean userExcluded;
    private String exclusionMessage;
}

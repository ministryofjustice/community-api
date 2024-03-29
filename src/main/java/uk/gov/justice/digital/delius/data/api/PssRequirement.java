package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PssRequirement {
    private KeyValue type;
    private KeyValue subType;
    @Schema(description = "Is the requirement currently active")
    private Boolean active;
}

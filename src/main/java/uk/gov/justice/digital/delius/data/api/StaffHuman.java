package uk.gov.justice.digital.delius.data.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffHuman {
    @Schema(description = "Staff code", example = "AN001A")
    private String code;
    @Schema(description = "Given names", example = "Sheila Linda")
    private String forenames;
    @Schema(description = "Family name", example = "Hancock")
    private String surname;
    @JsonProperty(access = Access.READ_ONLY)
    public boolean isUnallocated() {
        return Optional.ofNullable(code).map(c -> c.endsWith("U")).orElse(false);
    }
}

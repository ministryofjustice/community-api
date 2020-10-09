package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "Staff code", example = "AN001A")
    private String code;
    @ApiModelProperty(value = "Given names", example = "Sheila Linda")
    private String forenames;
    @ApiModelProperty(value = "Family name", example = "Hancock")
    private String surname;

    public boolean isUnallocated() {
        return Optional.ofNullable(code).map(c -> c.endsWith("U")).orElse(false);
    }
}

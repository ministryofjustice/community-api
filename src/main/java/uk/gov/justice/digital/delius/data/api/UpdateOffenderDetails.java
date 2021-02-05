package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOffenderDetails {
    @NotBlank(message = "Missing firstname")
    @ApiModelProperty(example = "John")
    private String firstName;
    @NotBlank(message = "Missing surname")
    @ApiModelProperty(example = "Smith")
    private String surname;
}

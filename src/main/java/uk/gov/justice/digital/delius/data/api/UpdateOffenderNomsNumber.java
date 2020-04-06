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
public class UpdateOffenderNomsNumber {
    @ApiModelProperty(value = "NOMS number to be set on the conviction. AKA offenderNo", example = "G5555TT")
    @NotBlank(message = "Missing a NOMS number")
    private String nomsNumber;
}

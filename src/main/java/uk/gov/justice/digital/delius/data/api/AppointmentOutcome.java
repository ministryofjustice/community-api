package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentOutcome {
    @NotNull
    @ApiModelProperty(name = "Code", example = "ABC123")
    private String code;

    @NotNull
    @ApiModelProperty(name = "Description", example = "Some appointment outcome")
    private String description;

    @ApiModelProperty(name = "Attended", example = "true")
    private Boolean attended;

    @ApiModelProperty(name = "Complied", example = "true")
    private Boolean complied;

    @ApiModelProperty(name = "Hours credited", example = "1.5")
    private Double hoursCredited;
}

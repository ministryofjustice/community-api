package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderAssessments {

    @ApiModelProperty(value = "Risk of Serious Recidivism")
    private Double rsrScore;
    @ApiModelProperty(value = "Offender Group Reconviction Scale")
    private Integer ogrsScore;
    @ApiModelProperty(example = "1982-10-24")
    private LocalDate ogrsLastUpdate;
}

package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Risk of Serious Recidivism")
    private Double rsrScore;
    @Schema(description = "Offender Group Reconviction Scale")
    private Integer ogrsScore;
    @Schema(example = "1982-10-24")
    private LocalDate ogrsLastUpdate;
}

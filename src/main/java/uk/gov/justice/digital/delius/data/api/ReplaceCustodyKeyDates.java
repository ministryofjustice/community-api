package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
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
@ApiModel(description = "Any dates not supplied will be removed from the associated conviction")
public class ReplaceCustodyKeyDates {
    @ApiModelProperty(value = "Conditional release date", example = "2020-06-23")
    private LocalDate conditionalReleaseDate;
    @ApiModelProperty(value = "Licence expiry date", example = "2020-06-23")
    private LocalDate licenceExpiryDate;
    @ApiModelProperty(value = "Home detention curfew eligibility date", example = "2020-06-23")
    private LocalDate hdcEligibilityDate;
    @ApiModelProperty(value = "Parole eligibility date", example = "2020-06-23")
    private LocalDate paroleEligibilityDate;
    @ApiModelProperty(value = "Sentence expiry date", example = "2020-06-23")
    private LocalDate sentenceExpiryDate;
    @ApiModelProperty(value = "Expected release date", example = "2020-06-23")
    private LocalDate expectedReleaseDate;
    @ApiModelProperty(value = "Post sentence Supervision end date. AKA Top-up supervision end data", example = "2020-06-23")
    private LocalDate postSentenceSupervisionEndDate;
}

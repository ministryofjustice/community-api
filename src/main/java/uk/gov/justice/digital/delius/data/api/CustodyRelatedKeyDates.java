package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel("Key sentence dates that are related to their time in custody")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustodyRelatedKeyDates {
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
    @ApiModelProperty(value = "Post sentence Supervision end date. AKA Top-up supervision end date", example = "2020-06-23")
    private LocalDate postSentenceSupervisionEndDate;
    @ApiModelProperty(value = "Expected start date of the handover process from prison offender manager to community offender manager", example = "2020-06-23")
    private LocalDate expectedPrisonOffenderManagerHandoverStartDate;
    @ApiModelProperty(value = "Expected actual handover date from prison offender manager to community offender manager", example = "2020-06-23")
    private LocalDate expectedPrisonOffenderManagerHandoverDate;
}

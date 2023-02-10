package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Key sentence dates that are related to their time in custody")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustodyRelatedKeyDates {
    @Schema(description = "Conditional release date", example = "2020-06-23")
    private LocalDate conditionalReleaseDate;
    @Schema(description = "Licence expiry date", example = "2020-06-23")
    private LocalDate licenceExpiryDate;
    @Schema(description = "Home detention curfew eligibility date", example = "2020-06-23")
    private LocalDate hdcEligibilityDate;
    @Schema(description = "Parole eligibility date", example = "2020-06-23")
    private LocalDate paroleEligibilityDate;
    @Schema(description = "Sentence expiry date", example = "2020-06-23")
    private LocalDate sentenceExpiryDate;
    @Schema(description = "Expected release date", example = "2020-06-23")
    private LocalDate expectedReleaseDate;
    @Schema(description = "Post sentence Supervision end date. AKA Top-up supervision end date", example = "2020-06-23")
    private LocalDate postSentenceSupervisionEndDate;
    @Schema(description = "Expected start date of the handover process from prison offender manager to community offender manager", example = "2020-06-23")
    private LocalDate expectedPrisonOffenderManagerHandoverStartDate;
    @Schema(description = "Expected actual handover date from prison offender manager to community offender manager", example = "2020-06-23")
    private LocalDate expectedPrisonOffenderManagerHandoverDate;
}

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
@Schema(description = "Any dates not supplied will be removed from the associated conviction")
public class ReplaceCustodyKeyDates {
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
    @Schema(description = "Post sentence Supervision end date. AKA Top-up supervision end data", example = "2020-06-23")
    private LocalDate postSentenceSupervisionEndDate;
}

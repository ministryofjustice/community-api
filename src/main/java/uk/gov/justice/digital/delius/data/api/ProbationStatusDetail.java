package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProbationStatusDetail {
    private final ProbationStatus status;
    @Schema(description = "The termination date of the most recently terminated sentence")
    private final LocalDate previouslyKnownTerminationDate;
    @Schema(description = "True if the offender is in breach of a current sentence")
    private final Boolean inBreach;
    @Schema(description = "True if the offender has a conviction with no sentence")
    private final Boolean preSentenceActivity;
    @Schema(description = "True if the offender has a event with no sentence which has been adjourned for a pre-sentence report")
    private final Boolean awaitingPsr;
}

package uk.gov.justice.digital.delius.data.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProbationStatusDetail {
    private final ProbationStatus status;
    @ApiModelProperty(value = "The termination date of the most recently terminated sentence")
    private final LocalDate previouslyKnownTerminationDate;
    @ApiModelProperty(value = "True if the offender is in breach of a current sentence")
    private final Boolean inBreach;
    @ApiModelProperty(value = "True if the offender has a conviction with no sentence")
    private final Boolean preSentenceActivity;
}

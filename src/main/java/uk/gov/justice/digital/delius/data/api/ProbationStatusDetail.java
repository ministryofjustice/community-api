package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProbationStatusDetail {
    private final ProbationStatus probationStatus;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean inBreach;
    private final Boolean preSentenceActivity;
}

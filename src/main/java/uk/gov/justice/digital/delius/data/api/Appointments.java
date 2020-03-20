package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Appointments {
    private Long total;
    private Long attended;
    private Long acceptableAbsences;
    private Long unacceptableAbsences;
    private Long noOutcomeRecorded;
}

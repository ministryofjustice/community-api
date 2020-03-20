package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnpaidWork {
    private Long minutesOrdered;
    private Long minutesCompleted;
    private Appointments appointments;
}

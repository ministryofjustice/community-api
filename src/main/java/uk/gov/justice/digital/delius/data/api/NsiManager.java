package uk.gov.justice.digital.delius.data.api;



import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class NsiManager {
    private ProbationArea probationArea;
    private Team team;
    private LocalDate startDate;
    private LocalDate endDate;
}

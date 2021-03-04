package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;

@With
@Getter
@Builder
public class NsiManager {
    private ProbationArea probationArea;
    private Team team;
    private StaffDetails staff;
    private LocalDate startDate;
    private LocalDate endDate;
}

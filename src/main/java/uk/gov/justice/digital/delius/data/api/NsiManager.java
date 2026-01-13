package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@With
@Getter
@Setter
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class NsiManager {
    private ProbationArea probationArea;
    private Team team;
    private StaffDetails staff;
    private LocalDate startDate;
    private LocalDate endDate;
}

package uk.gov.justice.digital.delius.data.api.deliusapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplaceContact {
    private String offenderCrn;
    private String outcome;
    private String officeLocation;  // Null location means no change
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long nsiId;
    private Long eventId;
    private Long requirementId;
}

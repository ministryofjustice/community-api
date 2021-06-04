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
public class NewContact {
    private String offenderCrn;
    private String type;
    private String outcome;
    private String provider;
    private String team;
    private String staff;
    private String officeLocation;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime  endTime;
    private Boolean alert;
    private Boolean sensitive;
    private Boolean rarActivity;
    private String notes;
    private String description;
    private Long nsiId;
    private Long eventId;
    private Long requirementId;
}

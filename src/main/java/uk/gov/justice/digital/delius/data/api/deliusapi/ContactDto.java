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
public class ContactDto {
    private Long id;
    private String offenderCrn;
    private Long nsiId;
    private String type;
    private String typeDescription;
    private String outcome;
    private String outcomeDescription;
    private String enforcement;
    private String enforcementDescription;
    private String provider;
    private String providerDescription;
    private String team;
    private String teamDescription;
    private String staff;
    private String staffFirstName;
    private String staffLastName;
    private String officeLocation;
    private String officeLocationDescription;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean alert;
    private Boolean sensitive;
    private String notes;
    private String description;
    private Long eventId;
    private Long requirementId;
}

package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registration {
    private Long registrationId;
    private Long offenderId;
    private KeyValue register;
    private KeyValue type;
    private String riskColour;
    private LocalDate startDate;
    private LocalDate nextReviewDate;
    private Long reviewPeriodMonths;
    private String notes;
    private KeyValue registeringTeam;
    private Human registeringOfficer;
    private KeyValue registeringProbationArea;
    private KeyValue registerLevel;
    private KeyValue registerCategory;
    private boolean warnUser;
    private boolean active;
    private LocalDate endDate;
    private KeyValue deregisteringTeam;
    private Human deregisteringOfficer;
    private KeyValue deregisteringProbationArea;
    private String deregisteringNotes;
}

package uk.gov.justice.digital.delius.data.api.deliusapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewNsi {

    private String type;

    private String subType;

    private String offenderCrn;

    private Long eventId;

    private Long requirementId;

    private LocalDate referralDate;

    private LocalDate expectedStartDate;

    private LocalDate expectedEndDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long length;

    private String status;

    private LocalDateTime statusDate;

    private String outcome;

    private String notes;

    private String intendedProvider;

    private NewNsiManager manager;
}

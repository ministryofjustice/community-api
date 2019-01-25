package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Disability {
    private Long disabilityId;
    private KeyValue disabilityType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
}

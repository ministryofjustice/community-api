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
public class Disability {
    private Long disabilityId;
    private KeyValue disabilityType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
}

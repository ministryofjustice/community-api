package uk.gov.justice.digital.delius.data.api.deliusapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class NewRecall {
    private Long id;
    private LocalDate recallDate;
    private String recallReason;
    private String institution;
    private String notes;
}

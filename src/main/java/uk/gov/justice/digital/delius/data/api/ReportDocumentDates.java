package uk.gov.justice.digital.delius.data.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReportDocumentDates {

    private LocalDate requestedDate;
    private LocalDate requiredDate;
    private LocalDateTime completedDate;
}

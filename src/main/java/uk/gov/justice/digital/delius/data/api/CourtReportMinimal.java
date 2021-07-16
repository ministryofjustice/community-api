package uk.gov.justice.digital.delius.data.api;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class CourtReportMinimal {
    private final Long courtReportId;
    private final Long offenderId;
    private final LocalDateTime requestedDate;
    private final LocalDateTime requiredDate;
    private final LocalDateTime allocationDate;
    private final LocalDateTime completedDate;
    private final LocalDateTime sentToCourtDate;
    private final LocalDateTime receivedByCourtDate;
    private final KeyValue courtReportType;
    private final List<ReportManager> reportManagers;
}

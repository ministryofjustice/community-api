package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CourtAppearance {
    private Long courtAppearanceId;
    private LocalDate appearanceDate;
    private Court court;
    private List<CourtReport> courtReports;
}

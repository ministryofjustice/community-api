package uk.gov.justice.digital.delius.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CourtAppearance {
    private Long courtAppearanceId;
    private LocalDateTime appearanceDate;
    private String crownCourtCalendarNumber;
    private String bailConditions;
    private String courtNotes;
    private Long eventId;
    private Long teamId;
    private Long staffId;
    private Court court;
    private Long appearanceTypeId;
    private Long pleaId;
    private KeyValue outcome;
    private Long remandStatusId;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastUpdatedDatetime;
    private Long offenderId;
    private List<CourtReport> courtReports;
    private List<String> offenceIds;
}

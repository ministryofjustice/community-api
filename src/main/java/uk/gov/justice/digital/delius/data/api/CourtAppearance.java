package uk.gov.justice.digital.delius.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CourtAppearance {
    private Long courtAppearanceId;
    private LocalDateTime appearanceDate;
    private String crownCourtCalendarNumber;
    private String bailConditions;
    private String courtNotes;
    private Long eventId;
    private Long teamId;
    private Long staffId;
    private Boolean softDeleted;
    private Court court;
    private Long appearanceTypeId;
    private Long pleaId;
    private Long outcomeId;
    private Long remandStatusId;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastUpdatedDatetime;
    private Long offenderId;
    private List<CourtReport> courtReports;
}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "COURT_APPEARANCE")
public class CourtAppearance {

    @Id
    @Column(name = "COURT_APPEARANCE_ID")
    private Long courtAppearanceId;

    @Column(name = "APPEARANCE_DATE")
    private LocalDateTime appearanceDate;

    @Column(name = "CROWN_COURT_CALENDAR_NUMBER")
    private String crownCourtCalendarNumber;

    @Column(name = "BAIL_CONDITIONS")
    private String bailConditions;

    @Column(name = "COURT_NOTES")
    private String courtNotes;

    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "STAFF_ID")
    private Long staffId;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @JoinColumn(name = "COURT_ID")
    @ManyToOne
    private Court court;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @Column(name = "APPEARANCE_TYPE_ID")
    private Long appearanceTypeId;

    @Column(name = "PLEA_ID")
    private Long pleaId;

    @Column(name = "OUTCOME_ID")
    private Long outcomeId;

    @Column(name = "REMAND_STATUS_ID")
    private Long remandStatusId;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @OneToMany(mappedBy = "courtAppearance")
    private List<CourtReport> courtReports;
}

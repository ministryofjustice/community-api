package uk.gov.justice.digital.delius.jpa.standard.entity;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "COURT_REPORT")
@ToString(exclude="courtAppearance")
public class CourtReport {
    @Id
    @Column(name = "COURT_REPORT_ID")
    private Long courtReportId;

    @ManyToOne
    @JoinColumn(name = "COURT_APPEARANCE_ID", referencedColumnName="COURT_APPEARANCE_ID")
    private CourtAppearance courtAppearance;

    @Column(name = "DATE_REQUESTED")
    private LocalDateTime dateRequested;

    @Column(name = "DATE_REQUIRED")
    private LocalDateTime dateRequired;

    @Column(name = "ALLOCATION_DATE")
    private LocalDateTime allocationDate;

    @Column(name = "COMPLETED_DATE")
    private LocalDateTime completedDate;

    @Column(name = "SENT_TO_COURT_DATE")
    private LocalDateTime sentToCourtDate;

    @Column(name = "RECEIVED_BY_COURT_DATE")
    private LocalDateTime receivedByCourtDate;

    @OneToMany(mappedBy = "courtReport", fetch = FetchType.EAGER)
    private List<ReportManager> reportManagers;

    @Column(name = "VIDEO_LINK")
    private String videoLink;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "PUNISHMENT")
    private String punishment;

    @Column(name = "REDUCTION_OF_CRIME")
    private String reductionOfCrime;

    @Column(name = "REFORM_AND_REHABILITATION")
    private String reformAndRehabilitation;

    @Column(name = "PUBLIC_PROTECTION")
    private String publicProtection;

    @Column(name = "REPARATION")
    private String reparation;

    @Column(name = "RECOMMENDATIONS_NOT_STATED")
    private String recommendationsNotStated;

    @Column(name = "SOFT_DELETED")
    private boolean softDeleted;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @Column(name = "LEVEL_OF_SERIOUSNESS_ID")
    private Long levelOfSeriousnessId;

    @Column(name = "DELIVERED_REPORT_REASON_ID")
    private Long deliveredReportReasonId;

    @Column(name = "SECTION_178")
    private String section178;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

    @Column(name = "COURT_REPORT_TYPE_ID")
    private Long courtReportTypeId;

    @ManyToOne
    @JoinColumn(name = "COURT_REPORT_TYPE_ID", updatable = false, insertable = false)
    private RCourtReportType courtReportType;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "DELIVERED_COURT_REPORT_TYPE_ID")
    private Long deliveredCourtReportTypeId;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @JoinColumn(name = "REQUIRED_BY_COURT_ID", referencedColumnName = "COURT_ID")
    @OneToOne
    private Court requiredByCourt;

    @Column(name = "PENDING_TRANSFER")
    private Long pendingTransfer;
}

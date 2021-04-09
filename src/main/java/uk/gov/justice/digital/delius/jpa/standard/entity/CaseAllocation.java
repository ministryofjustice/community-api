package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CASE_ALLOCATION")
public class CaseAllocation {
    @Id@Column(name = "CASE_ALLOCATION_ID")
    private Long caseAllocationId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    private Event event;
    @Column(name = "RSR_SCORE")
    private Long rsrScore;
    @Column(name = "RSR_ASSESSOR_PROVIDER_ID")
    private Long rsrAssessorProviderId;
    @Column(name = "RSR_ASSESSOR_TEAM_ID")
    private Long rsrAssessorTeamId;
    @Column(name = "RSR_ASSESSOR_STAFF_ID")
    private Long rsrAssessorStaffId;
    @Column(name = "RSR_ASSESSOR_DATE")
    private LocalDateTime rsrAssessorDate;
    @JoinColumn(name = "ALLOCATION_DECISION_ID")
    @ManyToOne
    private StandardReference allocationDecision;
    @Column(name = "ALLOCATION_DECISION_DATE")
    private LocalDateTime allocationDecisionDate;
    @Column(name = "TARGET_PROVIDER_ID")
    private Long targetProviderId;
    @Column(name = "DECISION_PROVIDER_ID")
    private Long decisionProviderId;
    @Column(name = "DECISION_TEAM_ID")
    private Long decisionTeamId;
    @Column(name = "DECISION_STAFF_ID")
    private Long decisionStaffId;
    @Column(name = "ALLOCATION_DECISION_NOTES")
    private String allocationDecisionNotes;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "ALLOCATION_OVERRIDE_REASON_ID")
    private Long allocationOverrideReasonId;
    @Column(name = "ALLOCATION_OVERRIDE")
    private Long allocationOverride;
    @Column(name = "OSP_SCORE")
    private Long ospScore;


}

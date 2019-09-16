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
@Table(name = "APPROVED_PREMISES_REFERRAL")
public class ApprovedPremisesReferral {
    @Id@Column(name = "APPROVED_PREMISES_REFERRAL_ID")
    private Long approvedPremisesReferralId;
    @Column(name = "REFERRAL_DATE")
    private LocalDateTime referralDate;
    @Column(name = "EXPECTED_ARRIVAL_DATE")
    private LocalDateTime expectedArrivalDate;
    @Column(name = "EXPECTED_DEPARTURE_DATE")
    private LocalDateTime expectedDepartureDate;
    @Column(name = "DECISION_DATE")
    private LocalDateTime decisionDate;
    @Column(name = "REFERRAL_NOTES")
    private String referralNotes;
    @Column(name = "NON_ARRIVAL_DATE")
    private LocalDateTime nonArrivalDate;
    @Column(name = "ORIGINAL_AP_ADMIT_DATE")
    private LocalDateTime originalApAdmitDate;
    @Column(name = "SELF_HARM_ISSUES")
    private String selfHarmIssues;
    @Column(name = "NON_ARRIVAL_NOTES")
    private String nonArrivalNotes;
    @Column(name = "REFERRING_TEAM_ID")
    private Long referringTeamId;
    @Column(name = "DECISION_BY_TEAM_ID")
    private Long decisionByTeamId;
    @Column(name = "REFERRING_STAFF_ID")
    private Long referringStaffId;
    @Column(name = "DECISION_BY_STAFF_ID")
    private Long decisionByStaffId;
    @Column(name = "DECISION_NOTES")
    private String decisionNotes;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "REFERRAL_CATEGORY_ID")
    private Long referralCategoryId;
    @Column(name = "REFERRAL_DECISION_ID")
    private Long referralDecisionId;
    @Column(name = "PENDING_STATUS_ID")
    private Long pendingStatusId;
    @Column(name = "REJECT_REASON_ID")
    private Long rejectReasonId;
    @Column(name = "REFERRAL_OUTCOME_ID")
    private Long referralOutcomeId;
    @Column(name = "REFERRAL_GROUP_ID")
    private Long referralGroupId;
    @Column(name = "NON_ARRIVAL_REASON_ID")
    private Long nonArrivalReasonId;
    @Column(name = "INSTITUTION_ID")
    private Long institutionId;
    @Column(name = "APPROVED_PREMISES_ID")
    private Long approvedPremisesId;
    @Column(name = "TRANSFER_REASON_ID")
    private Long transferReasonId;
    @Column(name = "REFERRAL_SOURCE_ID")
    private Long referralSourceId;
    @Column(name = "SOURCE_TYPE_ID")
    private Long sourceTypeId;
    @Column(name = "EXTERNAL_REFERRAL_REASON_ID")
    private Long externalReferralReasonId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    private Event event;
    @Column(name = "ESTABLISHMENT")
    private String establishment;
    @Column(name = "REFERRING_PROBATION_AREA_ID")
    private Long referringProbationAreaId;
    @Column(name = "REFERRED_TO_PROBATION_AREA_ID")
    private Long referredToProbationAreaId;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "ORIGINAL_APPROVED_PREMISES_ID")
    private Long originalApprovedPremisesId;
    @Column(name = "RESERVATION_START_DATE")
    private LocalDateTime reservationStartDate;
    @Column(name = "RESERVATION_LENGTH")
    private Long reservationLength;


}

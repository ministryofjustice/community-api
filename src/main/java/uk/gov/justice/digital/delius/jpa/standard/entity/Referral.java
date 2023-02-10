package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Referral {
    @Id@Column(name = "REFERRAL_ID")
    private Long referralId;
    @Column(name = "REFERRAL_DATE")
    private LocalDateTime referralDate;
    @Column(name = "ATTENDED")
    private String attended;
    @Column(name = "SOURCE_NOTES")
    private String sourceNotes;
    @Column(name = "NOTES")
    private String notes;
    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    private Event event;
    @Column(name = "REF_TO_TEAM_ID")
    private Long refToTeamId;
    @Column(name = "REF_TO_STAFF_ID")
    private Long refToStaffId;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "REF_TO_PROVIDER_EMPLOYEE_ID")
    private Long refToProviderEmployeeId;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "TREATMENT_AIM_ID")
    private Long treatmentAimId;
    @Column(name = "REF_TO_PROVIDER_TEAM_ID")
    private Long refToProviderTeamId;
    @JoinColumn(name = "REFERRAL_TYPE_ID")
    @ManyToOne
    private RReferralType referralType;
    @Column(name = "REFERRAL_SOURCE_ID")
    private Long referralSourceId;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "GENERIC_REFERRAL_OUTCOME_ID")
    private Long genericReferralOutcomeId;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "DOCUMENT_LINKED")
    private String documentLinked;


}

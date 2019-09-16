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
public class Assessment {
    @Id@Column(name = "ASSESSMENT_ID")
    private Long assessmentId;
    @JoinColumn(name = "REFERRAL_ID")
    @ManyToOne
    private Referral referral;
    @Column(name = "ASSESSMENT_DATE")
    private LocalDateTime assessmentDate;
    @Column(name = "OFFENDERS_AGREEMENT")
    private String offendersAgreement;
    @Column(name = "NOTES")
    private String notes;
    @Column(name = "TEAM_ID")
    private Long teamId;
    @Column(name = "STAFF_ID")
    private Long staffId;
    @Column(name = "PROVIDER_EMPLOYEE_ID")
    private Long providerEmployeeId;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "OFFENDER_REQUIRED_TO_ATTEND")
    private String offenderRequiredToAttend;
    @Column(name = "OFFENDER_ATTENDED")
    private String offenderAttended;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @JoinColumn(name = "ASSESSMENT_TYPE_ID")
    @ManyToOne
    private RAssessmentType assessmentType;
    @Column(name = "PROVIDER_TEAM_ID")
    private Long providerTeamId;
    @Column(name = "SCORE")
    private String score;
    @Column(name = "DURATION_MINUTES")
    private Long durationMinutes;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "ASSESSMENT_OUTCOME_ID")
    private Long assessmentOutcomeId;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "DOCUMENT_LINKED")
    private String documentLinked;


}

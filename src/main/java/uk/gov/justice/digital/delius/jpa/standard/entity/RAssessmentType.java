package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "R_ASSESSMENT_TYPE")
public class RAssessmentType {
    @Id@Column(name = "ASSESSMENT_TYPE_ID")
    private Long assessmentTypeId;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "SELECTABLE")
    private String selectable;
    @Column(name = "ASSESSMENT_OUTCOME_FLAG")
    private String assessmentOutcomeFlag;
    @Column(name = "DURATION_FLAG")
    private String durationFlag;
    @Column(name = "SCORE_FLAG")
    private String scoreFlag;
    @Column(name = "OFFENDERS_AGREEMENT_FLAG")
    private String offendersAgreementFlag;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "CONTACT_TYPE_ID")
    private Long contactTypeId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

}

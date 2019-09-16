package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ADDRESS_ASSESSMENT")
public class AddressAssessment {
    @Id@Column(name = "ADDRESS_ASSESSMENT_ID")
    private Long addressAssessmentId;
    @Column(name = "ASSESSMENT_DATE")
    private LocalDateTime assessmentDate;
    @Column(name = "DETAILS")
    private String details;
    @Column(name = "STAFF_ID")
    private Long staffId;
    @Column(name = "TEAM_ID")
    private Long teamId;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "PROVIDER_EMPLOYEE_ID")
    private Long providerEmployeeId;
    @Column(name = "PROVIDER_TEAM_ID")
    private Long providerTeamId;
    @Column(name = "OFFENDER_ADDRESS_ID")
    private Long offenderAddressId;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "DOCUMENT_LINKED")
    private String documentLinked;


}

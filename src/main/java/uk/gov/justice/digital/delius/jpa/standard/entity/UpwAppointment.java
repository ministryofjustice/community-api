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
@Table(name = "UPW_APPOINTMENT")
public class UpwAppointment {
    @Id@Column(name = "UPW_APPOINTMENT_ID")
    private Long upwAppointmentId;
    @JoinColumn(name = "UPW_DETAILS_ID")
    @ManyToOne
    private UpwDetails upwDetails;
    @JoinColumn(name = "UPW_PROJECT_ID")
    @ManyToOne
    private UpwProject upwProject;
    @Column(name = "APPOINTMENT_DATE")
    private LocalDateTime appointmentDate;
    @Column(name = "START_TIME")
    private LocalDateTime startTime;
    @Column(name = "END_TIME")
    private LocalDateTime endTime;
    @Column(name = "MINUTES_OFFERED")
    private Long minutesOffered;
    @Column(name = "MINUTES_CREDITED")
    private Long minutesCredited;
    @Column(name = "TRAVEL_TIME")
    private Long travelTime;
    @Column(name = "ATTENDED")
    private String attended;
    @Column(name = "COMPLIED")
    private String complied;
    @Column(name = "HIGH_VISIBILITY_VEST")
    private String highVisibilityVest;
    @Column(name = "NOTES")
    private String notes;
    @Column(name = "TEAM_ID")
    private Long teamId;
    @Column(name = "STAFF_ID")
    private Long staffId;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "WORK_QUALITY_ID")
    private Long workQualityId;
    @Column(name = "BEHAVIOUR_ID")
    private Long behaviourId;
    @Column(name = "PENALTY_TIME")
    private Long penaltyTime;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "UPW_OUTCOME_ID")
    private Long upwOutcomeId;
    @Column(name = "CONTACT_OUTCOME_TYPE_ID")
    private Long contactOutcomeTypeId;
    @Column(name = "CONTACT_ID")
    private Long contactId;
    @Column(name = "PROVIDER_TEAM_ID")
    private Long providerTeamId;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "PROVIDER_EMPLOYEE_ID")
    private Long providerEmployeeId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "PROJECT_NAME")
    private String projectName;
    @Column(name = "PROJECT_TYPE_ID")
    private Long projectTypeId;
    @Column(name = "INTENSIVE")
    private String intensive;
    @Column(name = "DOCUMENT_LINKED")
    private String documentLinked;


}

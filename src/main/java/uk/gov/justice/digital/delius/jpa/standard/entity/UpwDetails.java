package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UPW_DETAILS")
public class UpwDetails {
    @Id@Column(name = "UPW_DETAILS_ID")
    private Long upwDetailsId;
    @JoinColumn(name = "DISPOSAL_ID")
    @OneToOne
    private Disposal disposal;
    @Column(name = "UPW_LENGTH_MINUTES")
    private Long upwLengthMinutes;
    @Column(name = "AGREED_TRAVEL_FARE")
    private Long agreedTravelFare;
    @Column(name = "NOTES")
    private String notes;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @ManyToOne
    @JoinColumn(name = "UPW_STATUS_ID")
    private StandardReference status;
    @Column(name = "WORKED_INTENSIVELY")
    private String workedIntensively;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "TEAM_ID")
    private Long teamId;
    @Column(name = "PROVIDER_TEAM_ID")
    private Long providerTeamId;
    @Column(name = "STAFF_ID")
    private Long staffId;
    @Column(name = "UPW_STATUS_DATE")
    private LocalDateTime upwStatusDate;
    @Column(name = "PROVIDER_EMPLOYEE_ID")
    private Long providerEmployeeId;
    @Column(name = "ALLOCATION_DATE")
    private LocalDateTime allocationDate;
    @Column(name = "MINUTES_YEAR1")
    private Long minutesYear1;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @OneToMany(mappedBy = "upwDetails")
    private List<UpwAppointment> appointments;



}

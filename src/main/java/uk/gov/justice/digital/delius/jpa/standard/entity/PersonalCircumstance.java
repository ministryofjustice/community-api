package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PERSONAL_CIRCUMSTANCE")
public class PersonalCircumstance {
    @Id@Column(name = "PERSONAL_CIRCUMSTANCE_ID")
    private Long personalCircumstanceId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "START_DATE")
    private LocalDate startDate;
    @Column(name = "END_DATE")
    private LocalDate endDate;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @JoinColumn(name = "CIRCUMSTANCE_TYPE_ID")
    @OneToOne
    private CircumstanceType circumstanceType;
    @JoinColumn(name = "CIRCUMSTANCE_SUB_TYPE_ID")
    @OneToOne
    private CircumstanceSubType circumstanceSubType;
    @Column(name = "NOTES")
    private String notes;
    @Column(name = "EVIDENCED")
    private String evidenced;
    @Column(name = "QUALIFICATION_1_ID")
    private Long qualification1Id;
    @Column(name = "QUALIFICATION_2_ID")
    private Long qualification2Id;
    @Column(name = "QUALIFICATION_3_ID")
    private Long qualification3Id;
    @Column(name = "OFFENDER_ADDRESS_ID")
    private Long offenderAddressId;
    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;
}

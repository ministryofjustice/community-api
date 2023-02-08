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
@Table(name = "UPW_PROJECT")
public class UpwProject {
    @Id@Column(name = "UPW_PROJECT_ID")
    private Long upwProjectId;
    @Column(name = "CODE")
    private String code;
    @Column(name = "NAME")
    private String name;
    @Column(name = "BENEFICIARY")
    private String beneficiary;
    @Column(name = "HIGH_VISIBILITY_VEST_REQUIRED")
    private String highVisibilityVestRequired;
    @Column(name = "ACTUAL_START_DATE")
    private LocalDateTime actualStartDate;
    @Column(name = "COMPLETION_DATE")
    private LocalDateTime completionDate;
    @Column(name = "EXPECTED_END_DATE")
    private LocalDateTime expectedEndDate;
    @Column(name = "BENEFICIARY_CONTACT_ADDRESS_ID")
    private Long beneficiaryContactAddressId;
    @Column(name = "PLACEMENT_ADDRESS_ID")
    private Long placementAddressId;
    @Column(name = "BENEFICIARY_ADDRESS")
    private String beneficiaryAddress;
    @Column(name = "BENEFICIARY_CONTACT_NAME")
    private String beneficiaryContactName;
    @Column(name = "SELECTABLE")
    private String selectable;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "PROJECT_TYPE_ID")
    private Long projectTypeId;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "TEAM_ID")
    private Long teamId;
    @Column(name = "PROVIDER_TEAM_ID")
    private Long providerTeamId;
    @Column(name = "PROBATION_AREA_ID")
    private Long probationAreaId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "COMMISSIONING_SOURCE_1")
    private Long commissioningSource1;
    @Column(name = "COMMISSIONING_SOURCE_2")
    private Long commissioningSource2;
    @Column(name = "COMMISSIONING_SOURCE_3")
    private Long commissioningSource3;
    @Column(name = "COMMISSIONING_SOURCE_4")
    private Long commissioningSource4;
    @Column(name = "PLACEMENT_NOTES")
    private String placementNotes;
    @Column(name = "BENEFICIARY_NOTES")
    private String beneficiaryNotes;
    @Column(name = "PLACEMENT_CONTACT_NAME")
    private String placementContactName;
    @Column(name = "COMMISSIONING_SOURCE_5")
    private Long commissioningSource5;
    @Column(name = "UPW_DIVERSITY_1")
    private Long upwDiversity1;
    @Column(name = "UPW_DIVERSITY_2")
    private Long upwDiversity2;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "UPW_DIVERSITY_3")
    private Long upwDiversity3;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "UPW_DIVERSITY_4")
    private Long upwDiversity4;
    @Column(name = "UPW_DIVERSITY_5")
    private Long upwDiversity5;

}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "CONTACT")
public class Contact {

    @Id
    @SequenceGenerator(name = "CONTACT_ID_GENERATOR", sequenceName = "CONTACT_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTACT_ID_GENERATOR")
    @Column(name = "CONTACT_ID")
    private Long contactId;

    @Column(name = "LINKED_CONTACT_ID")
    private Long linkedContactId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @JoinColumn(name = "CONTACT_TYPE_ID")
    @OneToOne
    private ContactType contactType;

    @JoinColumn(name = "RQMNT_ID")
    @OneToOne
    private Requirement requirement;

    @JoinColumn(name = "EXPLANATION_ID")
    @OneToOne
    private Explanation explanation;

    @JoinColumn(name = "LIC_CONDITION_ID")
    @OneToOne
    private LicenceCondition licenceCondition;

    @JoinColumn(name = "NSI_ID")
    @OneToOne
    private Nsi nsi;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "CONTACT_START_TIME")
    private LocalTime contactStartTime;

    @Column(name = "CONTACT_DATE")
    private LocalDate contactDate;

    @Column(name = "CONTACT_END_TIME")
    private LocalTime contactEndTime;

    @Column(name = "SOFT_DELETED")
    @Builder.Default
    private Long softDeleted = 0L;

    @Column(name = "TRUST_PROVIDER_FLAG")
    @Builder.Default
    private Long trustProviderFlag = 0L;

    @Column(name = "ALERT_ACTIVE")
    private String alertActive;

    @JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID")
    @OneToOne
    private ContactOutcomeType contactOutcomeType;

    @JoinColumn(name = "PROVIDER_LOCATION_ID")
    @OneToOne
    private ProviderLocation providerLocation;

    @JoinColumn(name = "OFFICE_LOCATION_ID")
    @OneToOne
    private OfficeLocation officeLocation;

    @JoinColumn(name = "PROVIDER_EMPLOYEE_ID")
    @OneToOne
    private ProviderEmployee providerEmployee;

    @JoinColumn(name = "PROVIDER_TEAM_ID")
    @OneToOne
    private ProviderTeam providerTeam;

    @JoinColumn(name = "STAFF_ID")
    @OneToOne
    private Staff staff;

    @JoinColumn(name = "TEAM_ID")
    @OneToOne
    private Team team;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;

    @Column(name = "PARTITION_AREA_ID")
    @Builder.Default
    private Long partitionAreaId = 0L;

    @Column(name = "STAFF_EMPLOYEE_ID")
    private Long staffEmployeeId;

    @Column(name = "TRUST_PROVIDER_TEAM_ID")
    private Long teamProviderId;

    @JoinColumn(name = "PARTITION_AREA_ID", updatable = false, insertable = false)
    @OneToOne
    private PartitionArea partitionArea;

    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    private Event event;

    @Column(name = "HOURS_CREDITED")
    private Double hoursCredited;

    @Column(name = "VISOR_CONTACT")
    @Builder.Default
    private String visorContact = "N";

    @Column(name = "VISOR_EXPORTED")
    @Builder.Default
    private String visorExported = "N";

    @Column(name = "ATTENDED")
    private String attended;

    @Column(name = "COMPLIED")
    private String complied;

    @Column(name = "ENFORCEMENT")
    private String enforcement;

    @Column(name = "DOCUMENT_LINKED")
    private String documentLinked;

    @Column(name = "UPLOAD_LINKED")
    private String uploadLinked;

    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;

    @Column(name = "CREATED_BY_USER_ID")
    @CreatedBy
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    @CreatedDate
    private LocalDateTime createdDateTime;

    @Column(name = "LAST_UPDATED_USER_ID")
    @LastModifiedBy
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    @LastModifiedDate
    private LocalDateTime lastUpdatedDateTime;

    @Column(name = "SENSITIVE")
    private String sensitive;

    @Column(name = "RAR_ACTIVITY")
    private String rarActivity;

    public boolean isRarActivity() {
        return "Y".equals(rarActivity);
    }
}

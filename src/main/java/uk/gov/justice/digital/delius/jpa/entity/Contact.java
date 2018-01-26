package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CONTACT")
public class Contact {

    @Id
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
    private LocalDateTime contactStartTime;

    @Column(name = "CONTACT_END_TIME")
    private LocalDateTime contactEndTime;

    @Column(name = "SOFT_DELETED")
    private Boolean softDeleted;

    @Column(name = "ALERT_ACTIVE")
    private String alertActive;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDateTime;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDateTime;

    @JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID")
    @OneToOne
    private ContactOutcomeType contactOutcomeType;

    @JoinColumn(name = "PROVIDER_LOCATION_ID")
    @OneToOne
    private ProviderLocation providerLocation;

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

    @JoinColumn(name = "PARTITION_AREA_ID")
    @OneToOne
    private PartitionArea partitionArea;

    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    private Event event;


}

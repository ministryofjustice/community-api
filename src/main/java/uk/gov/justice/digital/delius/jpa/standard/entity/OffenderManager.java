package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "OFFENDER_MANAGER")
public class OffenderManager {

    @Column(name = "OFFENDER_MANAGER_ID")
    @Id
    private Long offenderManagerId;

    @JoinColumn(name = "TEAM_ID")
    @OneToOne
    private Team team;

    @JoinColumn(name = "ALLOCATION_STAFF_ID")
    @OneToOne
    private Staff staff;

    @JoinColumn(name = "PARTITION_AREA_ID")
    @OneToOne
    private PartitionArea partitionArea;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @JoinColumn(name = "PROVIDER_EMPLOYEE_ID")
    @OneToOne
    private ProviderEmployee providerEmployee;

    @JoinColumn(name = "PROVIDER_TEAM_ID")
    @OneToOne
    private ProviderTeam providerTeam;

    @JoinColumns({
            @JoinColumn(name = "STAFF_EMPLOYEE_ID", referencedColumnName = "STAFF_EMPLOYEE_ID", insertable = false, updatable = false),
            @JoinColumn(name = "TRUST_PROVIDER_FLAG", referencedColumnName = "TRUST_PROVIDER_FLAG", insertable = false, updatable = false)
    })
    @OneToOne
    private Officer officer;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;

    @JoinColumns({
            @JoinColumn(name = "TRUST_PROVIDER_TEAM_ID", referencedColumnName = "TRUST_PROVIDER_TEAM_ID", insertable = false, updatable = false),
            @JoinColumn(name = "TRUST_PROVIDER_FLAG", referencedColumnName = "TRUST_PROVIDER_FLAG", insertable = false, updatable = false)
    })
    @OneToOne
    private AllTeam trustProviderTeam;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @Column(name = "ALLOCATION_DATE")
    private Timestamp allocationDate;

    @Column(name = "END_DATE")
    private Timestamp endDate;

    @JoinColumn(name = "ALLOCATION_REASON_ID")
    @OneToOne
    private StandardReference allocationReason;

}

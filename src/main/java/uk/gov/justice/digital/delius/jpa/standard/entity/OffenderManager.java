package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(of = {"offenderManagerId", "offenderId" , "allocationDate"})
@ToString(exclude = {"team","staff","partitionArea","providerTeam","probationArea", "responsibleOfficers","managedOffender" ,"officer"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "OFFENDER_MANAGER")
public class OffenderManager implements Serializable {

    @Column(name = "OFFENDER_MANAGER_ID")
    @Id
    private Long offenderManagerId;

    @OneToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    @OneToOne
    @JoinColumn(name = "ALLOCATION_STAFF_ID")
    private Staff staff;

    @OneToOne
    @JoinColumn(name = "PARTITION_AREA_ID")
    private PartitionArea partitionArea;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @OneToOne
    @JoinColumn(name = "PROVIDER_EMPLOYEE_ID")
    private ProviderEmployee providerEmployee;

    @OneToOne
    @JoinColumn(name = "PROVIDER_TEAM_ID")
    private ProviderTeam providerTeam;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "STAFF_EMPLOYEE_ID", referencedColumnName = "STAFF_EMPLOYEE_ID", insertable = false, updatable = false),
            @JoinColumn(name = "TRUST_PROVIDER_FLAG", referencedColumnName = "TRUST_PROVIDER_FLAG", insertable = false, updatable = false)
    })
    private Officer officer;

    @OneToOne
    @JoinColumn(name = "PROBATION_AREA_ID")
    private ProbationArea probationArea;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "TRUST_PROVIDER_TEAM_ID", referencedColumnName = "TRUST_PROVIDER_TEAM_ID", insertable = false, updatable = false),
            @JoinColumn(name = "TRUST_PROVIDER_FLAG", referencedColumnName = "TRUST_PROVIDER_FLAG", insertable = false, updatable = false)
    })
    private AllTeam trustProviderTeam;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @Column(name = "ALLOCATION_DATE")
    private LocalDate allocationDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @OneToOne
    @JoinColumn(name = "ALLOCATION_REASON_ID")
    private StandardReference allocationReason;

    @OneToMany
    @JoinColumns({
            @JoinColumn(name = "OFFENDER_MANAGER_ID", referencedColumnName = "OFFENDER_MANAGER_ID", insertable = false, updatable = false),
            @JoinColumn(name = "OFFENDER_ID",
                    referencedColumnName = "OFFENDER_ID", insertable = false, updatable = false)
    })
    @Builder.Default
    private List<ResponsibleOfficer> responsibleOfficers = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "OFFENDER_ID", referencedColumnName = "OFFENDER_ID", insertable = false, updatable = false)
    // Only select OFFENDER rows that have SOFT_DELETED != 1
    @Where(clause = "SOFT_DELETED != 1")
    private Offender managedOffender;

    public boolean isActive() {
        return endDate == null && Optional.ofNullable(activeFlag).orElse(0L) == 1L && !isDeleted();
    }
    public ResponsibleOfficer getActiveResponsibleOfficer() {
        return responsibleOfficers.stream().filter(ResponsibleOfficer::isActive).findAny().orElse(null);
    }
    public ResponsibleOfficer getLatestResponsibleOfficer() {
        return responsibleOfficers.stream().max(Comparator.comparing(ResponsibleOfficer::getStartDateTime)).orElse(null);
    }
    public void addResponsibleOfficer(ResponsibleOfficer responsibleOfficer) {
        responsibleOfficers.add(responsibleOfficer);
    }

    private boolean isDeleted() {
        return Optional.ofNullable(softDeleted).orElse(0L) == 1L;
    }
}

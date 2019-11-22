package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Optional;

@EqualsAndHashCode(of = "prisonOffenderManagerId")
@ToString(exclude = {"team","staff","probationArea", "responsibleOfficer" ,"managedOffender"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "PRISON_OFFENDER_MANAGER")
public class PrisonOffenderManager {

    @Id
    @Column(name = "PRISON_OFFENDER_MANAGER_ID")
    private Long prisonOffenderManagerId;

    @OneToOne
    @JoinColumn(name = "ALLOCATION_TEAM_ID")
    private Team team;

    @OneToOne
    @JoinColumn(name = "ALLOCATION_STAFF_ID")
    private Staff staff;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @OneToOne
    @JoinColumn(name = "PROBATION_AREA_ID")
    private ProbationArea probationArea;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @Column(name = "ALLOCATION_DATE")
    private Timestamp allocationDate;

    @Column(name = "END_DATE")
    private Timestamp endDate;

    @OneToOne
    @JoinColumn(name = "ALLOCATION_REASON_ID")
    private StandardReference allocationReason;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "PRISON_OFFENDER_MANAGER_ID", referencedColumnName = "PRISON_OFFENDER_MANAGER_ID", insertable = false, updatable = false),
            @JoinColumn(name = "OFFENDER_ID", referencedColumnName = "OFFENDER_ID", insertable = false, updatable = false)
    })
    private ResponsibleOfficer responsibleOfficer;

    @OneToOne
    @JoinColumn(name = "OFFENDER_ID", referencedColumnName = "OFFENDER_ID", insertable = false, updatable = false)
    // Only select OFFENDER rows that have SOFT_DELETED != 1
    @Where(clause = "SOFT_DELETED != 1")
    private Offender managedOffender;

    public boolean isActive() {
        return endDate == null && Optional.ofNullable(activeFlag).orElse(0L) == 1L;
    }
}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

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

    @JoinColumn(name = "ALLOCATION_TEAM_ID")
    @OneToOne
    private Team team;

    @JoinColumn(name = "ALLOCATION_STAFF_ID")
    @OneToOne
    private Staff staff;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @Column(name = "ALLOCATION_DATE")
    private Timestamp allocationDate;

    @Column(name = "END_DATE")
    private Timestamp endDate;

    @JoinColumn(name = "ALLOCATION_REASON_ID")
    @OneToOne
    private StandardReference allocationReason;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "OFFENDER_MANAGER_ID", referencedColumnName = "PRISON_OFFENDER_MANAGER_ID"),
            @JoinColumn(name = "OFFENDER_ID", referencedColumnName = "OFFENDER_ID")
    })
    private ResponsibleOfficer responsibleOfficer;

    @OneToOne
    @JoinColumn(name = "OFFENDER_ID", referencedColumnName = "OFFENDER_ID", insertable = false, updatable = false)
    private Offender managedOffender;

}

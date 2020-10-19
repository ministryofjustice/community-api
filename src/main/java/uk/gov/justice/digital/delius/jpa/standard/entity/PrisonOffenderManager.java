package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@EqualsAndHashCode(of = "prisonOffenderManagerId")
@ToString(exclude = {"team","staff","probationArea", "responsibleOfficer" ,"managedOffender"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PRISON_OFFENDER_MANAGER")
public class PrisonOffenderManager {

    @Id
    @SequenceGenerator(name = "PRISON_OFFENDER_MANAGER_ID_GENERATOR", sequenceName = "PRISON_OFFENDER_MANAGER_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRISON_OFFENDER_MANAGER_ID_GENERATOR")
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
    @Builder.Default
    private Long softDeleted = 0L;

    @OneToOne
    @JoinColumn(name = "PROBATION_AREA_ID")
    private ProbationArea probationArea;

    @Column(name = "ACTIVE_FLAG")
    @Builder.Default
    private Long activeFlag = 1L;

    @Column(name = "ALLOCATION_DATE")
    @Builder.Default
    private LocalDate allocationDate = LocalDate.now();

    @Column(name = "END_DATE")
    private LocalDate endDate;

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

    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;

    @Column(name = "CREATED_BY_USER_ID")
    @CreatedBy
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    @CreatedDate
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    @LastModifiedBy
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    @LastModifiedDate
    private LocalDateTime lastUpdatedDatetime;


    public boolean isActive() {
        return endDate == null && Optional.ofNullable(activeFlag).orElse(0L) == 1L && !isDeleted();
    }
    private boolean isDeleted() {
        return Optional.ofNullable(softDeleted).orElse(0L) == 1L;
    }
}

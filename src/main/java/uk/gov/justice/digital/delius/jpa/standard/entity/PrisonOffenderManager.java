package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(of = "prisonOffenderManagerId")

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PRISON_OFFENDER_MANAGER")
public class PrisonOffenderManager implements Serializable {

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

    @OneToMany
    @JoinColumns({
            @JoinColumn(name = "PRISON_OFFENDER_MANAGER_ID", referencedColumnName = "PRISON_OFFENDER_MANAGER_ID", insertable = false, updatable = false),
            @JoinColumn(name = "OFFENDER_ID", referencedColumnName = "OFFENDER_ID", insertable = false, updatable = false)
    })
    @Builder.Default
    @ToString.Exclude
    private List<ResponsibleOfficer> responsibleOfficers = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "OFFENDER_ID", referencedColumnName = "OFFENDER_ID", insertable = false, updatable = false)
    // Only select OFFENDER rows that have SOFT_DELETED != 1
    @SQLRestriction("SOFT_DELETED != 1")
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

    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress;

    @Column(name = "TELEPHONE_NUMBER")
    private String telephoneNumber;

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

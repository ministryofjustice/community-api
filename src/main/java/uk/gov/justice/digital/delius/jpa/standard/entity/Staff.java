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
import java.util.List;

@EqualsAndHashCode(of = "staffId")
@ToString(exclude = {"offenderManagers", "prisonOffenderManagers"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "STAFF")
public class Staff {

    @Id
    @SequenceGenerator(name = "STAFF_ID_GENERATOR", sequenceName = "STAFF_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STAFF_ID_GENERATOR")
    @Column(name = "STAFF_ID")
    private Long staffId;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "FORENAME")
    private String forename;

    @Column(name = "FORENAME2")
    private String forname2;

    @Column(name = "OFFICER_CODE")
    private String officerCode;

    @OneToMany
    @JoinColumn(name = "ALLOCATION_STAFF_ID")
    // Only select rows from OFFENDER_MANAGER where they have ACTIVE = 1 and SOFT_DELETED != 1
    @Where(clause = "ACTIVE_FLAG = 1 AND SOFT_DELETED != 1")
    List<OffenderManager> offenderManagers;

    @OneToMany
    @JoinColumn(name = "ALLOCATION_STAFF_ID")
    // Only select rows from PRISON_OFFENDER_MANAGER where they have ACTIVE = 1 AND SOFT_DELETED != 1
    @Where(clause = "ACTIVE_FLAG = 1 AND SOFT_DELETED != 1")
    List<PrisonOffenderManager> prisonOffenderManagers;

    @ManyToMany
    @JoinTable(name = "STAFF_TEAM",
            joinColumns = { @JoinColumn(name="STAFF_ID", referencedColumnName="STAFF_ID")},
            inverseJoinColumns = {@JoinColumn(name="TEAM_ID", referencedColumnName="TEAM_ID")})
    private List<Team> teams;

    @OneToOne(mappedBy = "staff")
    private User user;

    @Column(name = "START_DATE")
    @Builder.Default
    private LocalDate startDate = LocalDate.now();

    @Column(name = "PRIVATE")
    private Long privateSector;

    @ManyToOne
    @JoinColumn(name = "TITLE_ID")
    private StandardReference title;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;

    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAFF_GRADE_ID")
    private StandardReference grade;

    public boolean isUnallocated() {
        return officerCode.endsWith("U") && !isInActive();
    }
    public boolean isInActive() {
        return officerCode.endsWith("IAVU");
    }
}

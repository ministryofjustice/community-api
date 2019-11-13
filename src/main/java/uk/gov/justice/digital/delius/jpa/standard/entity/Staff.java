package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(of = "staffId")
@ToString(exclude = {"offenderManagers", "prisonOffenderManagers"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "STAFF")
public class Staff {

    @Id
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
}

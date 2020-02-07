package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@ToString(exclude = {"providerTeams", "teams", "institution"})
@EqualsAndHashCode(of = "probationAreaId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "PROBATION_AREA")
public class ProbationArea {

    @Id
    @Column(name = "PROBATION_AREA_ID")
    private Long probationAreaId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID")
    private Organisation organisation;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "INSTITUTION_ID"),
            @JoinColumn(name = "ESTABLISHMENT")})
    private RInstitution institution;

    @OneToMany
    @JoinColumn(name = "PROBATION_AREA_ID")
    private List<Team> teams;

    @OneToMany
    @JoinColumn(name = "PROBATION_AREA_ID")
    private List<Borough> boroughs;

    @OneToMany
    @JoinColumn(name = "PROBATION_AREA_ID", referencedColumnName = "PROBATION_AREA_ID")
    private List<ProviderTeam> providerTeams;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "PRIVATE")
    private Long privateSector;

    @Column(name = "ESTABLISHMENT", updatable = false, insertable = false)
    private String establishment;

}

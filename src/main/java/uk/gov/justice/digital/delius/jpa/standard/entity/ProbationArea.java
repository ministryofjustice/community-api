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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @JoinColumn(name = "PROBATION_AREA_ID", referencedColumnName = "PROBATION_AREA_ID")
    private List<ProviderTeam> providerTeams;

    @Column(name = "END_DATE")
    private LocalDate endDate;

}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ALL_TEAM")
@IdClass(AllTeamPk.class)
public class AllTeam {

    @Column(name = "TRUST_PROVIDER_FLAG")
    private Long trustProvideFlag;

    @Id
    @Column(name = "TRUST_PROVIDER_TEAM_ID")
    private Long trustProviderTeamId;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TELEPHONE")
    private String telephone;

    @JoinColumn(name = "DISTRICT_ID")
    @OneToOne
    private District district;

}

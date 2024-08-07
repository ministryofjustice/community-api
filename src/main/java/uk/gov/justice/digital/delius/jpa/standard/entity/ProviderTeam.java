package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "PROVIDER_TEAM")
public class ProviderTeam {

    @Id
    @Column(name = "PROVIDER_TEAM_ID")
    private Long providerTeamId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PROBATION_AREA_ID")
    private Long probationAreaId;

    @OneToOne
    @JoinColumn(name = "EXTERNAL_PROVIDER_ID")
    private ExternalProvider externalProvider;

}

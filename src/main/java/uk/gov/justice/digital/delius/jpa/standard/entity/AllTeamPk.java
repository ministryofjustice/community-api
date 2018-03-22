package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@EqualsAndHashCode
public class AllTeamPk implements Serializable {
    @Id
    @Column(name = "TRUST_PROVIDER_FLAG")
    private Long trustProvideFlag;

    @Id
    @Column(name = "TRUST_PROVIDER_TEAM_ID")
    private Long trustProviderTeamId;
}

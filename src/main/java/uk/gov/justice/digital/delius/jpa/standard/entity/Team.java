package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import javax.persistence.*;

@EqualsAndHashCode(of = "teamId")
@ToString(exclude = {"district", "probationArea"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "TEAM")
public class Team {

    @Id
    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToOne
    @JoinColumn(name = "DISTRICT_ID")
    private District district;

    @OneToOne
    @JoinColumn(name = "LOCAL_DELIVERY_UNIT_ID")
    private LocalDeliveryUnit localDeliveryUnit;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;

    @OneToOne
    @JoinColumn(name = "SC_PROVIDER_ID")
    private ScProvider scProvider;

    @Column(name = "PRIVATE")
    private Long privateFlag;

    @Column(name = "TELEPHONE")
    private String telephone;

}

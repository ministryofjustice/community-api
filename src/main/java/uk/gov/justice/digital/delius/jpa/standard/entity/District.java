package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import javax.persistence.*;

@EqualsAndHashCode(of = "districtId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "DISTRICT")
public class District {

    @Id
    @Column(name = "DISTRICT_ID")
    private Long districtId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @JoinColumn(name = "BOROUGH_ID")
    @OneToOne
    private Borough borough;
}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Builder(toBuilder = true)
@Getter
@Entity
@Table(name = "NSI_MANAGER")
@NoArgsConstructor
@AllArgsConstructor
public class NsiManager {

    @Id
    @Column(name = "NSI_MANAGER_ID")
    private Long nsiManagerId;

    @ManyToOne
    @JoinColumn(name = "NSI_ID")
    private Nsi nsi;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;

    @JoinColumn(name = "TEAM_ID")
    @OneToOne
    private Team team;

    @JoinColumn(name = "STAFF_ID")
    @OneToOne
    private Staff staff;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;
}

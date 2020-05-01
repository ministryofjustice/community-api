package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "NSI")
public class Nsi {

    // TODO: 01/05/2020 Add foreign key NSI_MANAGER to retrieve probation area, team and officer 

    @Id
    @Column(name = "NSI_ID")
    private Long nsiId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    private Event event;

    @Column(name = "REFERRAL_DATE")
    private LocalDate referralDate;

    @Column(name = "ACTUAL_START_DATE")
    private LocalDate actualStartDate;

    @Column(name = "EXPECTED_START_DATE")
    private LocalDate expectedStartDate;

    @JoinColumn(name = "NSI_TYPE_ID")
    @OneToOne
    private NsiType nsiType;

    @JoinColumn(name = "NSI_SUB_TYPE_ID")
    @OneToOne
    private StandardReference nsiSubType;

    @JoinColumn(name = "RQMNT_ID")
    @OneToOne
    private Requirement rqmnt;

    @JoinColumn(name = "NSI_OUTCOME_ID")
    @OneToOne
    private StandardReference nsiOutcome;

    @JoinColumn(name = "NSI_STATUS_ID")
    @OneToOne
    private NsiStatus nsiStatus;

}

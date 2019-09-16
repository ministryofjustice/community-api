package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "NSI")
public class Nsi {

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

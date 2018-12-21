package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "EVENT")
public class Event {

    @Id
    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "IN_BREACH")
    private Long inBreach;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "EVENT_NUMBER")
    private String eventNumber;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @Column(name = "CONVICTION_DATE")
    private LocalDate convictionDate;

    @Column(name = "REFERRAL_DATE")
    private LocalDate referralDate;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @OneToOne(mappedBy = "event")
    private MainOffence mainOffence;

    @OneToMany(mappedBy = "event")
    private List<AdditionalOffence> additionalOffences;

    @OneToOne(mappedBy = "event")
    private Disposal disposal;

    @OneToMany(mappedBy = "event")
    private List<CourtAppearance> courtAppearances;

}

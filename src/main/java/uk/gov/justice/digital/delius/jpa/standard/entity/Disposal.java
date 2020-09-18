package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "DISPOSAL")
@ToString(exclude = {"custody", "event"})
public class Disposal {
    @Id
    @Column(name = "DISPOSAL_ID")
    private Long disposalId;

    @JoinColumn(name = "EVENT_ID", referencedColumnName = "EVENT_ID")
    @OneToOne
    private Event event;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "LENGTH")
    private Long length;

    @Column(name = "EFFECTIVE_LENGTH")
    private Long effectiveLength;

    @Column(name = "LENGTH_IN_DAYS")
    private Long lengthInDays;

    @Column(name = "ENTRY_LENGTH")
    private Long entryLength;

    @ManyToOne
    @JoinColumn(name = "ENTRY_LENGTH_UNITS_ID")
    private StandardReference entryLengthUnits;

    @Column(name = "LENGTH_2")
    private Long length2;

    @ManyToOne
    @JoinColumn(name = "ENTRY_LENGTH_2_UNITS_ID")
    private StandardReference entryLength2Units;

    @ManyToOne
    @JoinColumn(name = "DISPOSAL_TYPE_ID")
    private DisposalType disposalType;

    @OneToOne(mappedBy = "disposal")
    private Custody custody;

    @ManyToOne
    @JoinColumn(name = "DISPOSAL_TERMINATION_REASON_ID")
    private StandardReference terminationReason;

    @Column(name = "TERMINATION_DATE")
    private LocalDate terminationDate;

    @Column(name = "DISPOSAL_DATE")
    private LocalDate startDate;

    @OneToMany(targetEntity = Requirement.class, mappedBy = "disposal")
    private List<Requirement> requirements;

    @OneToOne(mappedBy = "disposal")
    private UpwDetails unpaidWorkDetails;

    @Column(name = "NOTIONAL_END_DATE")
    private LocalDate expectedSentenceEndDate;

    @Column(name = "ENTERED_NOTIONAL_END_DATE")
    private LocalDate enteredSentenceEndDate;

    @OneToMany(targetEntity = LicenceCondition.class, mappedBy = "disposal")
    private List<LicenceCondition> licenceConditions;

    public boolean isSoftDeleted() {
        return this.softDeleted != 0L;
    }

}

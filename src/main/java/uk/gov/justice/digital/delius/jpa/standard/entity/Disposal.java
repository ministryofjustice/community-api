package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "DISPOSAL")
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
}

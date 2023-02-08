package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CUSTODY_HISTORY")
public class CustodyHistory {
    @Id
    @SequenceGenerator(name = "CUSTODY_HISTORY_ID_GENERATOR", sequenceName = "CUSTODY_HISTORY_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CUSTODY_HISTORY_ID_GENERATOR")
    @Column(name = "CUSTODY_HISTORY_ID")
    private Long custodyHistoryId;

    @JoinColumn(name = "CUSTODY_EVENT_TYPE_ID")
    @ManyToOne
    private StandardReference custodyEventType;

    @Column(name = "DETAIL")
    private String detail;

    @JoinColumn(name = "OFFENDER_ID")
    @ManyToOne
    private Offender offender;

    @JoinColumn(name = "CUSTODY_ID")
    @ManyToOne
    private Custody custody;

    @Column(name = "HISTORICAL_DATE")
    private LocalDate when;

    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;

    @Column(name = "PARTITION_AREA_ID")
    @Builder.Default
    private Long partitionAreaId = 0L;


}

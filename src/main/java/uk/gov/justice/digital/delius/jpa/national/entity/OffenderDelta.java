package uk.gov.justice.digital.delius.jpa.national.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "OFFENDER_DELTA")
public class OffenderDelta {

    @Id
    @Column(name = "OFFENDER_DELTA_ID")
    private Long offenderDeltaId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "DATE_CHANGED")
    private LocalDateTime dateChanged;

    @Column(name = "ACTION")
    private String action;

    @Column(name = "SOURCE_TABLE")
    private String sourceTable;

    @Column(name = "SOURCE_RECORD_ID")
    private Long sourceRecordId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDateTime;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDateTime;

}

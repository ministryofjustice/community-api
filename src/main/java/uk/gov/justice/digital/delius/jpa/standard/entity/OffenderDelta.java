package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDateTime;

@Data
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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
    @Builder.Default
    private String status = "CREATED";

    @Column(name = "CREATED_DATETIME")
    @CreatedDate
    private LocalDateTime createdDateTime;

    @Column(name = "LAST_UPDATED_DATETIME")
    @LastModifiedDate
    @Version
    private LocalDateTime lastUpdatedDateTime;

    public OffenderDelta setInProgress() {
        this.setStatus("INPROGRESS");
        this.setLastUpdatedDateTime(LocalDateTime.now()); // if status is already INPROGRESS we still need to set last updated date time inside the cutoff, otherwise it is still considered a failed update
        return this;
    }

}

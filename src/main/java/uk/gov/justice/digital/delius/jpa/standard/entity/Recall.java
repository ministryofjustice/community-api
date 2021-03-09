package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "RECALL")
public class Recall {
    @Id
    @Column(name = "RECALL_ID")
    private Long releaseId;

    @Column(name = "SOFT_DELETED")
    @Builder.Default
    private Long softDeleted = 0L;

    @Column(name = "RECALL_DATE")
    private LocalDateTime recallDate;

    @Column(name = "NOTES")
    private String notes;

    @OneToOne
    @JoinColumn(name = "RECALL_REASON_ID")
    private RecallReason reason;

    public boolean isSoftDeleted() {
        return this.softDeleted != 0L;
    }

}

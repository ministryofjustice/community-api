package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
    private Long softDeleted;

    @Column(name = "RECALL_DATE")
    private LocalDateTime recallDate;

    @Column(name = "NOTES")
    private String notes;

}

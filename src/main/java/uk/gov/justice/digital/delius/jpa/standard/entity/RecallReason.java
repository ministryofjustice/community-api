package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "R_RECALL_REASON")
@ToString
public class RecallReason {
    @Id
    @Column(name = "RECALL_REASON_ID")
    private Long recallReasonId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SELECTABLE")
    private String selectable;

    public boolean isActive() {
        return "Y".equals(selectable);
    }
}

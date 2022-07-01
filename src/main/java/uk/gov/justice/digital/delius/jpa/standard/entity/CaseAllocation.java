package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CASE_ALLOCATION")
public class CaseAllocation {
    @Id
    @Column(name = "CASE_ALLOCATION_ID")
    private Long caseAllocationId;
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    private Event event;
    @Column(name = "RSR_SCORE")
    private Long rsrScore;
    @Column(name = "RSR_ASSESSOR_DATE")
    private LocalDateTime rsrAssessorDate;
    @JoinColumn(name = "ALLOCATION_DECISION_ID")
    @ManyToOne
    private StandardReference allocationDecision;
    @Column(name = "ALLOCATION_DECISION_DATE")
    private LocalDateTime allocationDecisionDate;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
    @Column(name = "OSP_SCORE")
    private Long ospScore;
}

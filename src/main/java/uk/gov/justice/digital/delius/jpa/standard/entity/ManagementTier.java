package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "MANAGEMENT_TIER")
public class ManagementTier {

    @Id
    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @ManyToOne
    @JoinColumn(name = "TIER_ID")
    private StandardReference tier;

    @Column(name = "DATE_CHANGED")
    private LocalDateTime dateChanged;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @Column(name = "SOFT_DELETED")
    private Integer softDeleted;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @ManyToOne
    @JoinColumn(name = "TIER_CHANGE_REASON_ID")
    private StandardReference tierChangeReason;

    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

    @Column(name = "CREATED_BY_USER_ID")
    @CreatedBy
    private Long createdByUserId;

    @Column(name = "LAST_UPDATED_USER_ID")
    @LastModifiedBy
    private Long lastUpdatedUserId;

    @Column(name = "IOM_NOMINAL")
    private String iomNominal;

    @Column(name = "SAFEGUARDING_ISSUE")
    private String safeguardingIssue;

    @Column(name = "VULNERABILITY_ISSUE")
    private String vulnerabilityIssue;

}

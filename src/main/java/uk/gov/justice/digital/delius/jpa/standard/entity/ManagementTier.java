package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
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

    @EmbeddedId ManagementTierId id;

    @Column(name = "PARTITION_AREA_ID")
    @Builder.Default
    private Long partitionAreaId = 0L;

    @Column(name = "SOFT_DELETED")
    @Builder.Default
    private Long softDeleted = 0L;

    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;

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

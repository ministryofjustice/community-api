package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@ToString(exclude = {"probationArea"})
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "LOCAL_DELIVERY_UNIT")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class LocalDeliveryUnit {
    @Id
    @SequenceGenerator(name = "LOCAL_DELIVERY_UNIT_ID_GENERATOR", sequenceName = "LOCAL_DELIVERY_UNIT_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOCAL_DELIVERY_UNIT_ID_GENERATOR")
    @Column(name = "LOCAL_DELIVERY_UNIT_ID")
    private Long localDeliveryUnitId;
    @Column(name = "CODE")

    private String code;
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SELECTABLE")
    @Builder.Default
    private String selectable = "Y";

    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;

    @Column(name = "CREATED_BY_USER_ID")
    @CreatedBy
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    @CreatedDate
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    @LastModifiedBy
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    @LastModifiedDate
    private LocalDateTime lastUpdatedDatetime;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    private ProbationArea probationArea;

    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

}

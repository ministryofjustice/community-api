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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString(exclude = {"probationArea"})
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "BOROUGH")
public class Borough {
    @Id
    @SequenceGenerator(name = "BOROUGH_ID_GENERATOR", sequenceName = "BOROUGH_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOROUGH_ID_GENERATOR")
    @Column(name = "BOROUGH_ID")
    private Long boroughId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SELECTABLE")
    @Builder.Default
    private String selectable = "Y";

    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

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

    @OneToMany
    @JoinColumn(name = "BOROUGH_ID")
    private List<District> districts;

    @ManyToMany
    @JoinTable(name = "R_LEVEL_2_HEAD_OF_LEVEL_2",
        joinColumns = @JoinColumn(name = "STAFF_ID"),
        inverseJoinColumns = @JoinColumn(name = "BOROUGH_ID"))
    private List<Staff> headsOfProbationDeliveryUnit;
}

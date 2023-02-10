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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Data
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
    @ToString.Exclude
    private ProbationArea probationArea;

    @OneToMany
    @JoinColumn(name = "BOROUGH_ID")
    @ToString.Exclude
    private List<District> districts;

    @ManyToMany
    @JoinTable(name = "R_LEVEL_2_HEAD_OF_LEVEL_2",
        joinColumns = @JoinColumn(name = "BOROUGH_ID"),
        inverseJoinColumns = @JoinColumn(name = "STAFF_ID"))
    @ToString.Exclude
    private List<Staff> headsOfProbationDeliveryUnit;
}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(of = "districtId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "DISTRICT")
public class District {

    @Id
    @SequenceGenerator(name = "DISTRICT_ID_GENERATOR", sequenceName = "DISTRICT_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DISTRICT_ID_GENERATOR")
    @Column(name = "DISTRICT_ID")
    private Long districtId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @JoinColumn(name = "BOROUGH_ID")
    @OneToOne
    private Borough borough;

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

    @OneToMany
    @JoinColumn(name = "DISTRICT_ID")
    @ToString.Exclude
    private List<Team> teams;
}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(of = "teamId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "TEAM")
public class Team {

    @Id
    @SequenceGenerator(name = "TEAM_ID_GENERATOR", sequenceName = "TEAM_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEAM_ID_GENERATOR")
    @Column(name = "TEAM_ID")
    private Long teamId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToOne
    @JoinColumn(name = "DISTRICT_ID")
    @ToString.Exclude
    private District district;

    @OneToOne
    @JoinColumn(name = "LOCAL_DELIVERY_UNIT_ID")
    private LocalDeliveryUnit localDeliveryUnit;

    @JoinColumn(name = "PROBATION_AREA_ID")
    @OneToOne
    @ToString.Exclude
    private ProbationArea probationArea;

    @OneToOne
    @JoinColumn(name = "SC_PROVIDER_ID")
    private ScProvider scProvider;

    @Column(name = "PRIVATE")
    private Long privateFlag;

    @Column(name = "TELEPHONE")
    private String telephone;

    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress;

    @Column(name = "UNPAID_WORK_TEAM")
    private String unpaidWorkTeam;

    @Column(name = "START_DATE")
    @Builder.Default
    private LocalDate startDate = LocalDate.now();

    @Column(name = "END_DATE")
    private LocalDate endDate;

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

    @ManyToMany
    @JoinTable(
        name = "TEAM_OFFICE_LOCATION",
        joinColumns = {@JoinColumn(name = "TEAM_ID", referencedColumnName = "TEAM_ID")},
        inverseJoinColumns = {@JoinColumn(name = "OFFICE_LOCATION_ID", referencedColumnName = "OFFICE_LOCATION_ID")}
    )
    @ToString.Exclude
    private List<OfficeLocation> officeLocations;
}

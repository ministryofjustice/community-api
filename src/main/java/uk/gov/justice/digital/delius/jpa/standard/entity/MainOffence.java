package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "MAIN_OFFENCE")
public class MainOffence {

    @Id
    @SequenceGenerator(name = "MAIN_OFFENCE_ID_GENERATOR", sequenceName = "MAIN_OFFENCE_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MAIN_OFFENCE_ID_GENERATOR")
    @Column(name = "MAIN_OFFENCE_ID")
    private Long mainOffenceId;

    @Column(name = "OFFENCE_DATE")
    private LocalDateTime offenceDate;

    @Column(name = "OFFENCE_COUNT")
    private Long offenceCount;

    @JoinColumn(name = "EVENT_ID")
    @OneToOne
    @ToString.Exclude
    private Event event;

    @Column(name = "TICS")
    private Long tics;

    @Column(name = "VERDICT")
    private String verdict;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @JoinColumn(name = "OFFENCE_ID")
    @OneToOne
    private Offence offence;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;
}

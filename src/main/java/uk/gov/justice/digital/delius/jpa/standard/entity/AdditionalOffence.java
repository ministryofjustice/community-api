package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ADDITIONAL_OFFENCE")
public class AdditionalOffence {

    @Id
    @SequenceGenerator(name = "ADDITIONAL_OFFENCE_ID_GENERATOR", sequenceName = "ADDITIONAL_OFFENCE_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADDITIONAL_OFFENCE_ID_GENERATOR")
    @Column(name = "ADDITIONAL_OFFENCE_ID")
    private Long additionalOffenceId;

    @Column(name = "OFFENCE_DATE")
    private LocalDateTime offenceDate;

    @Column(name = "OFFENCE_COUNT")
    private Long offenceCount;

    @JoinColumn(name = "EVENT_ID")
    @ManyToOne
    private Event event;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

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

    @JoinColumn(name = "OFFENCE_ID")
    @OneToOne
    private Offence offence;
}

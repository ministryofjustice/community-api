package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString(exclude = {"custody"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder(toBuilder = true)
@Table(name = "KEY_DATE")
public class KeyDate {
    @Id
    @SequenceGenerator(name = "KEY_DATE_ID_GENERATOR", sequenceName = "KEY_DATE_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "KEY_DATE_ID_GENERATOR")
    @Column(name = "KEY_DATE_ID")
    private Long keyDateId;
    @ManyToOne
    @JoinColumn(name = "CUSTODY_ID", nullable = false)
    private Custody custody;
    @ManyToOne
    @JoinColumn(name = "KEY_DATE_TYPE_ID")
    private StandardReference keyDateType;
    @Column(name = "KEY_DATE")
    private LocalDate keyDate;
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;
    @Column(name = "SOFT_DELETED")
    private Long softDeleted;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;
    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;
    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDatetime;

}

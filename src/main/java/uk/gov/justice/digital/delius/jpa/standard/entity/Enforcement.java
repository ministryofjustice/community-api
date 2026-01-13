package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalTime;

import static jakarta.persistence.GenerationType.AUTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "ENFORCEMENT")
@SQLRestriction("SOFT_DELETED = 0")
public class Enforcement extends AuditableEntity{

    @Id
    @SequenceGenerator(name = "ENFORCEMENT_ID_SEQ", sequenceName = "ENFORCEMENT_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = AUTO, generator = "ENFORCEMENT_ID_SEQ")
    @Column(name = "ENFORCEMENT_ID")
    private Long id;

    @Column(name = "RESPONSE_DATE")
    private LocalDate responseDate;

    @Column(name = "ACTION_TAKEN_DATE")
    private LocalDate actionTakenDate;

    @Column(name = "ACTION_TAKEN_TIME")
    private LocalTime actionTakenTime;

    @ManyToOne
    @JoinColumn(name = "ENFORCEMENT_ACTION_ID")
    private EnforcementAction enforcementAction;

    @Column(name = "PARTITION_AREA_ID", nullable = false)
    private Long partitionAreaId;

    @Column(name = "SOFT_DELETED", nullable = false)
    private Boolean softDeleted;

    @Column(name = "ROW_VERSION", nullable = false)
    @Version
    private Long rowVersion;

    @OneToOne
    @JoinColumn(name = "CONTACT_ID")
    private Contact contact;

}

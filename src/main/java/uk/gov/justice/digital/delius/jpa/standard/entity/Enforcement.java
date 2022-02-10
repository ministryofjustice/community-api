package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static javax.persistence.GenerationType.AUTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "ENFORCEMENT")
@Where(clause = "SOFT_DELETED = 0")
@EqualsAndHashCode(callSuper=false)
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

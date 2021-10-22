package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "R_ENFORCEMENT_ACTION")
public class EnforcementAction {

    @Id
    @Column(name = "ENFORCEMENT_ACTION_ID", nullable = false)
    private Long id;

    @Column(name = "CODE", length = 10, nullable = false)
    private String code;

    @Column(name = "DESCRIPTION", length = 50, nullable = false)
    private String description;

    @Column(name = "OUTSTANDING_CONTACT_ACTION")
    @Type(type = "yes_no")
    private Boolean outstandingContactAction;

    @Column(name = "RESPONSE_BY_PERIOD")
    private Long responseByPeriod;

    @ManyToOne
    @JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)
    private ContactType contactType;
}

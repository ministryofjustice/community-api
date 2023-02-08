package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
    @Convert(converter = YesNoConverter.class)
    private Boolean outstandingContactAction;

    @Column(name = "RESPONSE_BY_PERIOD")
    private Long responseByPeriod;

    @ManyToOne
    @JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)
    private ContactType contactType;
}

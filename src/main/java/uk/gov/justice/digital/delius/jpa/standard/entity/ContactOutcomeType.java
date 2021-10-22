package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "R_CONTACT_OUTCOME_TYPE")
public class ContactOutcomeType {

    @Id
    @Column(name = "CONTACT_OUTCOME_TYPE_ID")
    private long contactOutcomeTypeId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "OUTCOME_COMPLIANT_ACCEPTABLE", length = 1)
    @Type(type = "yes_no")
    private Boolean compliantAcceptable;

    @Column(name = "OUTCOME_ATTENDANCE", length = 1)
    @Type(type = "yes_no")
    private Boolean attendance;

    @Column(name = "ACTION_REQUIRED", nullable = false)
    @Type(type = "yes_no")
    private Boolean actionRequired;

    @Column(name = "ENFORCEABLE", length = 1)
    @Type(type = "yes_no")
    private Boolean enforceable;
}

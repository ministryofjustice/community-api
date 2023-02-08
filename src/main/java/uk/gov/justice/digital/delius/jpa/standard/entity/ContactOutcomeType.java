package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;

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
    @Convert(converter = YesNoConverter.class)
    private Boolean compliantAcceptable;

    @Column(name = "OUTCOME_ATTENDANCE", length = 1)
    @Convert(converter = YesNoConverter.class)
    private Boolean attendance;

    @Column(name = "ACTION_REQUIRED", nullable = false)
    @Convert(converter = YesNoConverter.class)
    private Boolean actionRequired;

    @Column(name = "ENFORCEABLE", length = 1)
    @Convert(converter = YesNoConverter.class)
    private Boolean enforceable;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "R_ENF_ACT_CONTACT_OUT_TYPE",
        joinColumns = {@JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID", nullable = false)},
        inverseJoinColumns = {@JoinColumn(name = "ENFORCEMENT_ACTION_ID", nullable = false)})
    private List<EnforcementAction> enforcementActions;
}

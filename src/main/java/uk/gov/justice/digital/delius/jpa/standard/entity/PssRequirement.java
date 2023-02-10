package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PSS_RQMNT")
public class PssRequirement extends AuditableEntity {
    @Id
    @Column(name = "PSS_RQMNT_ID")
    private Long pssRequirementId;

    @ManyToOne
    @JoinColumn(name = "CUSTODY_ID")
    private Custody custody;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @JoinColumn(name = "PSS_RQMNT_TYPE_MAIN_CAT_ID", referencedColumnName = "PSS_RQMNT_TYPE_MAIN_CAT_ID")
    @OneToOne
    private PssRequirementTypeMainCategory pssRequirementTypeMainCategory;

    @JoinColumn(name = "PSS_RQMNT_TYPE_SUB_CAT_ID", referencedColumnName = "PSS_RQMNT_TYPE_SUB_CAT_ID")
    @OneToOne
    private PssRequirementTypeSubCategory pssRequirementTypeSubCategory;
}

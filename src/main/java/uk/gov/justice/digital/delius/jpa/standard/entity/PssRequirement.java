package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

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

    @JoinColumn(name = "PSS_RQMNT_TYPE_SUB_CAT_ID", referencedColumnName = "STANDARD_REFERENCE_LIST_ID")
    @OneToOne
    private StandardReference pssRequirementTypeSubCategory;
}

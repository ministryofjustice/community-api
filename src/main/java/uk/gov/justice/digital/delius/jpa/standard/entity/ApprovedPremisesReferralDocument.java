package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import jakarta.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("APPROVED_PREMISES_REFERRAL")
public class ApprovedPremisesReferralDocument extends Document {
    @JoinColumn(name = "PRIMARY_KEY_ID", referencedColumnName = "APPROVED_PREMISES_REFERRAL_ID", insertable = false, updatable = false)
    @ManyToOne
    private ApprovedPremisesReferral approvedPremisesReferral;

}

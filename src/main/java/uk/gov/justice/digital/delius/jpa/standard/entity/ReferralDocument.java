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
@DiscriminatorValue("REFERRAL")
public class ReferralDocument extends Document {
    @JoinColumn(name = "PRIMARY_KEY_ID", referencedColumnName = "REFERRAL_ID", insertable = false, updatable = false)
    @ManyToOne
    private Referral referral;

}

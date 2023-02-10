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
@DiscriminatorValue("PERSONAL_CIRCUMSTANCE")
public class PersonalCircumstanceDocument extends Document {
    @JoinColumn(name = "PRIMARY_KEY_ID", referencedColumnName = "PERSONAL_CIRCUMSTANCE_ID", insertable = false, updatable = false)
    @ManyToOne
    private PersonalCircumstance personalCircumstance;

}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("PERSONALCONTACT")
public class PersonalContactDocument extends Document {
    @JoinColumn(name = "PRIMARY_KEY_ID", referencedColumnName = "PERSONAL_CONTACT_ID", insertable = false, updatable = false)
    @ManyToOne
    private PersonalContact personalContact;

}

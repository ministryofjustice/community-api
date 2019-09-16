package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@AllArgsConstructor
@Builder
@Entity
@EqualsAndHashCode(callSuper=true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("OFFENDER")
public class OffenderDocument extends Document {
}

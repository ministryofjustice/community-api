package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("EVENT")
public class EventDocument extends Document {
    @JoinColumn(name = "PRIMARY_KEY_ID", referencedColumnName = "EVENT_ID", insertable = false, updatable = false)
    @ManyToOne
    private Event event;

}

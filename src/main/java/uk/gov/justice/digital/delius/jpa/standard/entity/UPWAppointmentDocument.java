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
@DiscriminatorValue("UPW_APPOINTMENT")
public class UPWAppointmentDocument extends Document {
    @JoinColumn(name = "PRIMARY_KEY_ID", referencedColumnName = "UPW_APPOINTMENT_ID", insertable = false, updatable = false)
    @ManyToOne
    private UpwAppointment upwAppointment;

}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;

import jakarta.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "IAPS_EVENT")
public class IAPSEvent {
    @Id
    @Column(name = "EVENT_ID")
    private Long eventId;
    @Column(name = "IAPS_FLAG")
    private Long iapsFlag;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

}

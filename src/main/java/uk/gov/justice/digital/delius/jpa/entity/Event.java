package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "EVENT")
public class Event {

    @Id
    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "IN_BREACH")
    private Long inBreach;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;
}

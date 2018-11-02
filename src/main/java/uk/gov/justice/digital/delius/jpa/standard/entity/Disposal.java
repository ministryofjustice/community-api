package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "DISPOSAL")
public class Disposal {
    @Id
    @Column(name = "DISPOSAL_ID")
    private Long disposalId;

    @JoinColumn(name = "EVENT_ID", referencedColumnName = "EVENT_ID")
    @OneToOne
    private Event event;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;
}

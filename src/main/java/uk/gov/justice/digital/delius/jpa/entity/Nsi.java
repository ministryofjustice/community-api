package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "NSI")
public class Nsi {

    @Id
    @Column(name = "NSI_ID")
    private Long nsiId;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "EVENT_ID")
    private Long eventId;

    @JoinColumn(name = "NSI_TYPE_ID")
    @OneToOne
    private NsiType nsiType;

    @JoinColumn(name = "NSI_SUB_TYPE_ID")
    @OneToOne
    private StandardReference nsiSubType;

    @JoinColumn(name = "RQMNT_ID")
    @OneToOne
    private Requirement rqmnt;

}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;


@Data
public class RInstitutionPK implements Serializable {
    @Column(name = "INSTITUTION_ID")
    @Id
    private Long institutionId;
    @Column(name = "ESTABLISHMENT")
    @Id
    private String establishment;

}

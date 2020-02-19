package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderPrisonerPk implements Serializable {
    @Id
    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Id
    @Column(name = "PRISONER_NUMBER")
    private String prisonerNumber;
}

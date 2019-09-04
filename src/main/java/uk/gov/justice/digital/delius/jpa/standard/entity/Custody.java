package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CUSTODY")
public class Custody {
    @Id
    @Column(name = "CUSTODY_ID")
    private Long custodyId;

    @JoinColumn(name = "DISPOSAL_ID", referencedColumnName = "DISPOSAL_ID")
    @OneToOne
    private Disposal disposal;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "PRISONER_NUMBER")
    private String prisonerNumber;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "INSTITUTION_ID"),
            @JoinColumn(name = "ESTABLISHMENT")})
    private RInstitution institution;
}

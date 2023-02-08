package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "OFFENDER_PRISONER")
@IdClass(OffenderPrisonerPk.class)
public class OffenderPrisoner {

    @Id
    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Id
    @Column(name = "PRISONER_NUMBER")
    private String prisonerNumber;

    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;

    @Column(name = "PARTITION_AREA_ID")
    @Builder.Default
    private Long partitionAreaId = 0L;
}

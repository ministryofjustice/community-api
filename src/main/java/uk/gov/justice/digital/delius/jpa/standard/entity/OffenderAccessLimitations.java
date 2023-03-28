package uk.gov.justice.digital.delius.jpa.standard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "OFFENDER")
public class OffenderAccessLimitations {
    @Id
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "CRN")
    private String crn;
    @Column(name = "EXCLUSION_MESSAGE")
    private String exclusionMessage;
    @Column(name = "RESTRICTION_MESSAGE")
    private String restrictionMessage;
    @Column(name = "CURRENT_EXCLUSION", columnDefinition = "number")
    private Boolean currentExclusion;
    @Column(name = "CURRENT_RESTRICTION", columnDefinition = "number")
    private Boolean currentRestriction;
}

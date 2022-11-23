package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(exclude = {"offender"}, callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "ADDITIONAL_IDENTIFIER")
public class AdditionalIdentifier extends AuditableEntity {
    @Id
    @SequenceGenerator(name = "ADDITIONAL_IDENTIFIER_ID_GENERATOR", sequenceName = "ADDITIONAL_IDENTIFIER_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADDITIONAL_IDENTIFIER_ID_GENERATOR")
    @Column(name = "ADDITIONAL_IDENTIFIER_ID")
    private Long additionalIdentifierId;
    @Column(name = "IDENTIFIER")
    private String identifier;
    @ToString.Exclude
    @JoinColumn(name = "OFFENDER_ID")
    @ManyToOne
    private Offender offender;
    @Column(name = "SOFT_DELETED")
    @Builder.Default
    private Long softDeleted = 0L;
    @Column(name = "PARTITION_AREA_ID")
    @Builder.Default
    private Long partitionAreaId = 0L;
    @Column(name = "ROW_VERSION")
    @Builder.Default
    private Long rowVersion = 1L;
    @JoinColumn(name = "IDENTIFIER_NAME_ID")
    @ManyToOne
    private StandardReference identifierName;
    @Column(name = "TRAINING_SESSION_ID")
    private Long trainingSessionId;

    public boolean isDeleted() {
        return softDeleted == 1L;
    }
}

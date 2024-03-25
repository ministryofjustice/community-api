package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.List;
import java.util.Optional;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "R_DISPOSAL_TYPE")
public class DisposalType {
    private static final String nonStatutoryCustodyCode = "NC";
    private static final String statutoryCustodyCode = "SC";

    @Id
    @Column(name = "DISPOSAL_TYPE_ID")
    private Long disposalTypeId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CJA2003")
    @Convert(converter = YesNoConverter.class)
    private Boolean cja2003Order;

    @Column(name = "PRE_CJA2003")
    @Convert(converter = YesNoConverter.class)
    private Boolean  legacyOrder;

    @Column(name = "SENTENCE_TYPE")
    private String sentenceType;

    @Column(name = "FTC_LIMIT")
    private Long failureToComplyLimit;

    public boolean isCustodial() {
        return Optional
                .ofNullable(sentenceType)
                .filter(type -> List.of(nonStatutoryCustodyCode, statutoryCustodyCode).contains(type))
                .isPresent();
    }
}

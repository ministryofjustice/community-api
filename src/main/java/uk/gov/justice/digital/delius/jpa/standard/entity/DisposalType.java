package uk.gov.justice.digital.delius.jpa.standard.entity;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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

    @Column(name = "SENTENCE_TYPE")
    private String sentenceType;

    public boolean isCustodial() {
        return Optional
                .ofNullable(sentenceType)
                .filter(type -> ImmutableList.of(nonStatutoryCustodyCode, statutoryCustodyCode).contains(type))
                .isPresent();
    }
}

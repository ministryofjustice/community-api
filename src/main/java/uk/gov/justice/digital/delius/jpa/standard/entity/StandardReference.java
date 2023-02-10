package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "R_STANDARD_REFERENCE_LIST")
public class StandardReference {
    @Id
    @Column(name = "STANDARD_REFERENCE_LIST_ID")
    private Long standardReferenceListId;

    @Column(name = "CODE_VALUE")
    private String codeValue;

    @Column(name = "CODE_DESCRIPTION")
    private String codeDescription;

    @Column(name = "SELECTABLE")
    private String selectable;

    @ManyToOne
    @JoinColumn(name = "REFERENCE_DATA_MASTER_ID")
    @ToString.Exclude
    private ReferenceDataMaster referenceDataMaster;

    public boolean isActive() {
        return "Y".equals(selectable);
    }
}

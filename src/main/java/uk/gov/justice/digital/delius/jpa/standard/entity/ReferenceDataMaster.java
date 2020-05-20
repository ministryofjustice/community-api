package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "R_REFERENCE_DATA_MASTER")
public class ReferenceDataMaster {
    @Id
    @Column(name = "REFERENCE_DATA_MASTER_ID")
    private Long ReferenceDataMasterId;

    @Column(name = "CODE_SET_NAME")
    private String codeSetName;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToMany(mappedBy = "referenceDataMaster")
    private List<StandardReference> standardReferences;

}

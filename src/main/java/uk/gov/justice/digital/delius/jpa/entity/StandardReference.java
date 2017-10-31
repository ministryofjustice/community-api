package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
}

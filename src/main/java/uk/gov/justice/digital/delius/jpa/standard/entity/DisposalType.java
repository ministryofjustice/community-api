package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "R_DISPOSAL_TYPE")
public class DisposalType {
    @Id
    @Column(name = "DISPOSAL_TYPE_ID")
    private Long disposalTypeId;

    @Column(name = "DESCRIPTION")
    private String description;
}

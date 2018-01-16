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
@Table(name = "PARTITION_AREA")
public class PartitionArea {

    @Id
    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @Column(name = "AREA")
    private String area;
}

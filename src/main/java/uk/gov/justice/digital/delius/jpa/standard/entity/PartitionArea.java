package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

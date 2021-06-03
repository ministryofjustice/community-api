package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Provision
{
    @Id
    @Column(name = "PROVISION_ID")
    private Long provisionID;

    @Version
    @Column(name = "ROW_VERSION")
    private Long version;

    @ManyToOne()
    @JoinColumn(name = "DISABILITY_ID")
    private Disability disability;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "FINISH_DATE")
    private LocalDate finishDate;

    @ManyToOne()
    @JoinColumn(name = "PROVISION_TYPE_ID")
    private StandardReference provisionType;

    @Lob
    @Column(name = "NOTES")
    private String notes;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaID;
}

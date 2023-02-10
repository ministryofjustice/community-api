package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "LIC_CONDITION")
public class LicenceCondition {

    @Id
    @Column(name = "LIC_CONDITION_ID")
    private Long licenceConditionId;

    @Column(name = "LIC_CONDITION_NOTES")
    private String licenceConditionNotes;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "COMMENCEMENT_DATE")
    private LocalDate commencementDate;

    @Column(name = "COMMENCEMENT_NOTES")
    private String commencementNotes;

    @Column(name = "TERMINATION_DATE")
    private LocalDate terminationDate;

    @Column(name = "TERMINATION_NOTES")
    private String terminationNotes;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDateTime;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @JoinColumn(name = "LIC_COND_TYPE_MAIN_CAT_ID")
    @OneToOne
    private LicenceConditionTypeMainCat licenceConditionTypeMainCat;

    @JoinColumn(name = "LIC_COND_TYPE_SUB_CAT_ID")
    @ManyToOne
    private StandardReference licenceConditionTypeSubCat;

    @JoinColumn(name = "DISPOSAL_ID", referencedColumnName = "DISPOSAL_ID")
    @ManyToOne
    private Disposal disposal;

}

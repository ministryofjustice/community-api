package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "RQMNT")
public class Requirement {

    @Id
    @Column(name = "RQMNT_ID")
    private Long requirementId;

    @Column(name = "RQMNT_NOTES")
    private String requirementNotes;

    @Column(name = "COMMENCEMENT_DATE")
    private LocalDate commencementDate;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "TERMINATION_DATE")
    private LocalDate terminationDate;

    @Column(name = "EXPECTED_START_DATE")
    private LocalDate expectedStartDate;

    @Column(name = "EXPECTED_END_DATE")
    private LocalDate expectedEndDate;

    @Column(name = "ACTIVE_FLAG")
    private Long activeFlag;

    @JoinColumn(name = "RQMNT_TYPE_SUB_CATEGORY_ID")
    @OneToOne
    private StandardReference requirementTypeSubCategory;

    @JoinColumn(name = "AD_RQMNT_TYPE_MAIN_CATEGORY_ID")
    @OneToOne
    private AdRequirementTypeMainCategory adRequirementTypeMainCategory;

    @JoinColumn(name = "AD_RQMNT_TYPE_SUB_CATEGORY_ID")
    @OneToOne
    private StandardReference adRequirementTypeSubCategory;

    @JoinColumn(name = "RQMNT_TYPE_MAIN_CATEGORY_ID")
    @OneToOne
    private RequirementTypeMainCategory requirementTypeMainCategory;


}

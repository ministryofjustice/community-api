package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

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

    @JoinColumn(name = "DISPOSAL_ID", referencedColumnName = "DISPOSAL_ID")
    @ManyToOne
    private Disposal disposal;

    @JoinColumn(name = "RQMNT_TERMINATION_REASON_ID")
    @OneToOne()
    private StandardReference terminationReason;

    @Column(name = "LENGTH")
    private Long length;

    @Column(name = "RAR_COUNT")
    private Long rarCount;

    @Column(name = "SOFT_DELETED")
    private Long softDeleted = 0L;
}

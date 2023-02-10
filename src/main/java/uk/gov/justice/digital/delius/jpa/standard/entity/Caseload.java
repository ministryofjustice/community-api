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
import jakarta.persistence.Table;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "CASELOAD")
public class Caseload {
    @Id
    @Column(name = "CASELOAD_ID")
    private Long caseloadId;

    @ManyToOne
    @JoinColumn(name = "STAFF_EMPLOYEE_ID")
    private Staff staff;

    @ManyToOne
    @JoinColumn(name = "TRUST_PROVIDER_TEAM_ID")
    private Team team;

    @Column(name = "ALLOCATION_DATE")
    private LocalDate allocationDate;

    @Column(name = "ROLE_CODE")
    private String roleCode;

    @Column(name = "CRN")
    private String crn;

    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "OFFENDER_MANAGER_ID")
    private Long offenderManager;

    @Column(name = "EVENT_ID")
    private Long eventId;

    @Column(name = "ORDER_MANAGER_ID")
    private Long orderManagerId;

    @Column(name = "DISPOSAL_ID")
    private Long disposalId;

    @Column(name = "LIC_CONDITION_ID")
    private Long licConditionId;

    @Column(name = "LIC_CONDITION_MANAGER_ID")
    private Long licConditionManagerId;

    @Column(name = "RQMNT_ID")
    private Long rqmntId;

    @Column(name = "RQMNT_MANAGER_ID")
    private Long rqmntManagerId;

    @Column(name = "TRUST_PROVIDER_FLAG")
    private Long trustProviderFlag;

    @Column(name = "DISPOSAL_TYPE_ID")
    private Long disposalTypeId;

    @Column(name = "LIC_COND_TYPE_MAIN_CAT_ID")
    private Long licCondTypeMainCatId;

    @Column(name = "RQMNT_TYPE_MAIN_CATEGORY_ID")
    private Long rqmntTypeMainCategoryId;

    @Column(name = "AD_RQMNT_TYPE_MAIN_CATEGORY_ID")
    private Long adRqmntTypeMainCategoryId;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "GENDER_ID")
    private Long genderId;

    @Column(name = "SECOND_NAME")
    private String secondName;

    @Column(name = "THIRD_NAME")
    private String thirdName;

    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth;

    @Column(name = "CURRENT_HIGHEST_RISK_COLOUR")
    private String currentHighestRiskColour;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "CURRENT_EXCLUSION")
    private Long currentExclusion;

    @Column(name = "CURRENT_TIER")
    private Long currentTier;

    @Column(name = "CURRENT_RESTRICTION")
    private Long currentRestriction;

    @Column(name = "LENGTH_VALUE")
    private Long lengthValue;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @Column(name = "PARTITION_AREA_ID")
    private Long partitionAreaId;

    @Column(name = "PSS_RQMNT_ID")
    private Integer pssRqmntId;

    @Column(name = "PSS_RQMNT_MANAGER_ID")
    private Integer pssRqmntManagerId;

    @Column(name = "PSS_RQMNT_TYPE_MAIN_CAT_ID")
    private Integer pssRqmntTypeMainCatId;

    @Column(name = "LENGTH_IN_DAYS")
    private Long lengthInDays;

    @Column(name = "ENTRY_LENGTH_UNITS_ID")
    private Long entryLengthUnitsId;

    @Column(name = "NOTIONAL_END_DATE")
    private LocalDate notionalEndDate;

    @Column(name = "ENTRY_LENGTH")
    private Long entryLength;

    @Column(name = "NSI_ID")
    private Long nsiId;

    @Column(name = "NSI_MANAGER_ID")
    private Long nsiManagerId;

    @Column(name = "PRISON_OFFENDER_MANAGER_ID")
    private Long prisonOffenderManagerId;

    @Column(name = "REPORT_MANAGER_ID")
    private Long reportManagerId;

    @Column(name = "COURT_REPORT_ID")
    private Long courtReportId;

    @Column(name = "INSTITUTIONAL_REPORT_ID")
    private Long institutionalReportId;
}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Time;

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

    @Column(name = "DISPOSAL_TYPE_CODE")
    private String disposalTypeCode;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "SELECTABLE")
    private String selectable;

    @Column(name = "AUTO_TERMINATE")
    private String autoTerminate;

    @Column(name = "MINIMUM_LENGTH")
    private Long minimumLength;

    @Column(name = "MAXIMUM_LENGTH")
    private Long maximumLength;

    @Column(name = "CUSTODIAL_ORDER")
    private String custodialOrder;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @Column(name = "ORDER_ABBREVIATED_DESCRIPTION")
    private String orderAbbreviatedDescription;

    @Column(name = "PROPOSAL")
    private String proposal;

    @Column(name = "OUTCOME_ID")
    private Long outcomeId;

    @Column(name = "CJA2003")
    private String cja2003;

    @Column(name = "PRE_CJA2003")
    private String preCja2003;

    @Column(name = "FORM_20_CODE")
    private String form20Code;

    @Column(name = "SENTENCE_TYPE")
    private String sentenceType;

    @Column(name = "REQUIRED_INFORMATION")
    private String requiredInformation;

    @Column(name = "MINIMUM_AGE")
    private Long minimumAge;

    @Column(name = "MAXIMUM_AGE")
    private Long maximumAge;

    @Column(name = "CREATED_DATETIME")
    private Time createdDatetime;

    @Column(name = "LAST_UPDATED_DATETIME")
    private Time lastUpdatedDatetime;

    @Column(name = "UNITS_ID")
    private Long unitsId;

    @Column(name = "TURNOUT_FLAG")
    private String turnoutFlag;

    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;

    @Column(name = "COMPLIANCE_FLAG")
    private String complianceFlag;

    @Column(name = "LAST_UPDATED_USER_ID")
    private Long lastUpdatedUserId;

    @Column(name = "ENFORCEABLE_FLAG")
    private String enforceableFlag;

    @Column(name = "ACTION_FLAG")
    private String actionFlag;

    @Column(name = "FTC_LIMIT")
    private Long ftcLimit;

    @Column(name = "OUTCOME_WARNING")
    private Long outcomeWarning;

    @Column(name = "OUTCOME_MESSAGE")
    private String outcomeMessage;

    @Column(name = "COHORT")
    private String cohort;

    @Column(name = "PSS_RQMNT")
    private String pssRqmnt;

}

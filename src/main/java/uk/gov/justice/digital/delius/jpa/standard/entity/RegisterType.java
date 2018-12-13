package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "R_REGISTER_TYPE")
public class RegisterType {
    @Id
    @Column(name = "REGISTER_TYPE_ID")
    private Long registerTypeId;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "SELECTABLE")
    private String selectable;
    @Column(name = "ROW_VERSION")
    private Long rowVersion;
    @Column(name = "ALERT_MESSAGE_TEXT")
    private String alertMessageText;
    @Column(name = "RECORD_TRIGGER_OFFENCE")
    private String recordTriggerOffence;
    @Column(name = "RECORD_CATEGORY")
    private String recordCategory;
    @Column(name = "REGISTER_REVIEW_PERIOD")
    private Long registerReviewPeriod;
    @Column(name = "RECORD_LEVEL")
    private String recordLevel;
    @JoinColumn(name = "REGISTER_TYPE_FLAG_ID")
    @OneToOne
    private StandardReference registerTypeFlag;
    @Column(name = "ALERT_MESSAGE")
    private String alertMessage;
    @Column(name = "DPA_EXCLUDE")
    private String dpaExclude;
    @Column(name = "CREATED_BY_USER_ID")
    private Long createdByUserId;
    @Column(name = "COLOUR")
    private String colour;
    @Column(name = "DEFAULT_HEADINGS")
    private String defaultHeadings;
    @Column(name = "SPG_INTEREST")
    private Long spgInterest;
    @Column(name = "SPG_OVERRIDE")
    private Long spgOverride;
    @Column(name = "IOM_NOMINAL_INDICATOR")
    private Long iomNominalIndicator;
    @Column(name = "SAFEGUARDING_INDICATOR")
    private Long safeguardingIndicator;
    @Column(name = "VULNERABILITY_INDICATOR")
    private Long vulnerabilityIndicator;

}

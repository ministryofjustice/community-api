package uk.gov.justice.digital.delius.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Blob;
import java.sql.Clob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "OFFENDER")
public class Offender {

    @Id
    @Column(name = "OFFENDER_ID")
    private Long offenderId;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "CRN")
    private String crn;

    @Column(name = "SECOND_NAME")
    private String secondName;

    @Column(name = "PNC_NUMBER")
    private String pncNumber;

    @Column(name = "THIRD_NAME")
    private String thirdName;

    @Column(name = "CRO_NUMBER")
    private String croNumber;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "NOMS_NUMBER")
    private String nomsNumber;

    @Column(name = "PREVIOUS_SURNAME")
    private String previousSurname;

    @Column(name = "ALLOW_SMS")
    private String allowSMS;

    @Column(name = "DATE_OF_BIRTH_DATE")
    private LocalDate dateOfBirthDate;

    @Column(name = "NI_NUMBER")
    private String niNumber;

    @Column(name = "LANGUAGE_CONCERNS")
    private String languageConcerns;

    @Column(name = "DECEASED_DATE")
    private LocalDate deceasedDate;

    @Column(name = "INTERPRETER_REQUIRED")
    private String interpreterRequired;

    @Column(name = "IMMIGRATION_NUMBER")
    private String immigrationNumber;

    @Column(name = "EXCLUSION_MESSAGE")
    private String exclusionMessage;

    @Column(name = "RESTRICTION_MESSAGE")
    private String restrictionMessage;

    @Column(name = "TELEPHONE_NUMBER")
    private String telephoneNumber;

    @Column(name = "MOBILE_NUMBER")
    private String mobileNumber;

    @Column(name = "E_MAIL_ADDRESS")
    private String emailAddress;

    @Column(name = "SOFT_DELETED")
    private Boolean softDeleted;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @ManyToOne
    @JoinColumn(name = "TITLE_ID")
    private StandardReference title;

    @ManyToOne
    @JoinColumn(name = "GENDER_ID")
    private StandardReference gender;

    @ManyToOne
    @JoinColumn(name = "ETHNICITY_ID")
    private StandardReference ethnicity;

    @ManyToOne
    @JoinColumn(name = "NATIONALITY_ID")
    private StandardReference nationality;

    @ManyToOne
    @JoinColumn(name = "IMMIGRATION_STATUS_ID")
    private StandardReference immigrationStatus;

    @ManyToOne
    @JoinColumn(name = "LANGUAGE_ID")
    private StandardReference language;

    @ManyToOne
    @JoinColumn(name = "RELIGION_ID")
    private StandardReference religion;

    @Column(name = "MOST_RECENT_PRISONER_NUMBER")
    private String mostRecentPrisonerNumber;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDateTime;

    @Column(name = "LAST_UPDATED_DATETIME_DIVERSIT")
    private LocalDateTime lastUpdatedDateTimeDiversity;

    @ManyToOne
    @JoinColumn(name = "SECOND_NATIONALITY_ID")
    private StandardReference secondNationality;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    private List<OffenderAddress> offenderAddresses;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    private List<OffenderAlias> offenderAliases;

    @ManyToOne
    @JoinColumn(name = "SEXUAL_ORIENTATION_ID")
    private StandardReference sexualOrientation;

    @Column(name = "CURRENT_EXCLUSION")
    private Long currentExclusion;

    @Column(name = "CURRENT_DISPOSAL")
    private Long currentDisposal;

    @Column(name = "CURRENT_HIGHEST_RISK_COLOUR")
    private String currentHighestRiskColour;

    @Column(name = "CURRENT_RESTRICTION")
    private Long currentRestriction;

    @Column(name = "INSTITUTION_ID")
    private Long institutionId;

    @Column(name = "ESTABLISHMENT")
    private Character establishment;

    @Column(name = "PENDING_TRANSFER")
    private Long pendingTransfer;

    @Column(name = "OFFENDER_DETAILS")
    private Clob offenderDetails;

    @Column(name = "PREVIOUS_CONVICTION_DATE")
    private LocalDate previousConvictionDate;

    @Column(name = "PREVIOUS_CONVICTION_DOCUMENT")
    private Blob previousConvictionDocument;

    @Column(name = "PREV_CONVICTION_DOCUMENT_NAME")
    private String prevConvictionDocumentName;

    @Column(name = "CURRENT_REMAND_STATUS")
    private String currentRemandStatus;

    @ManyToOne
    @JoinColumn(name = "PARTITION_AREA_ID")
    private PartitionArea partitionArea;

}

package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(exclude = {"offenderManagers", "prisonOffenderManagers"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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
    private Long softDeleted;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TITLE_ID")
    private StandardReference title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GENDER_ID")
    private StandardReference gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ETHNICITY_ID")
    private StandardReference ethnicity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NATIONALITY_ID")
    private StandardReference nationality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IMMIGRATION_STATUS_ID")
    private StandardReference immigrationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LANGUAGE_ID")
    private StandardReference language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RELIGION_ID")
    private StandardReference religion;

    @Column(name = "MOST_RECENT_PRISONER_NUMBER")
    private String mostRecentPrisonerNumber;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDateTime;

    @Column(name = "LAST_UPDATED_DATETIME_DIVERSIT")
    private LocalDateTime lastUpdatedDateTimeDiversity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECOND_NATIONALITY_ID")
    private StandardReference secondNationality;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    // Only select OFFENDER_ADDRESS rows where SOFT_DELETED != 1
    @Where(clause="SOFT_DELETED != 1")
    private List<OffenderAddress> offenderAddresses;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    private List<OffenderAlias> offenderAliases;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    private List<Disability> disabilities;

    @ManyToOne(fetch = FetchType.LAZY)
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
    private String offenderDetails;

    @Column(name = "PREVIOUS_CONVICTION_DATE")
    private LocalDate previousConvictionDate;

    @Column(name = "PREV_CONVICTION_DOCUMENT_NAME")
    private String prevConvictionDocumentName;

    @Column(name = "PREV_CON_ALFRESCO_DOCUMENT_ID")
    private String previousConvictionsAlfrescoDocumentId;

    @JoinColumn(name = "PREV_CON_CREATED_BY_USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private User previousConvictionsCreatedByUser;

    @Column(name = "PREV_CON_CREATED_DATETIME")
    private LocalDateTime previousConvictionsCreatedDatetime;

    @Column(name = "CURRENT_REMAND_STATUS")
    private String currentRemandStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTITION_AREA_ID")
    private PartitionArea partitionArea;

    @OneToMany(mappedBy = "offenderId")
    // Only select OFFENDER_MANAGER rows where the ACTIVE_FLAG = 1 AND SOFT_DELETED != 1
    @Where(clause = "ACTIVE_FLAG = 1 AND SOFT_DELETED != 1")
    private List<OffenderManager> offenderManagers;

    @OneToMany(mappedBy = "offenderId")
    // Only select PRISON_OFFENDER_MANAGER rows where the ACTIVE_FLAG = 1 AND SOFT_DELETED= != 1
    @Where(clause = "ACTIVE_FLAG = 1 AND SOFT_DELETED != 1")
    private List<PrisonOffenderManager> prisonOffenderManagers;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "OFFENDER_ID")
    private List<AdditionalIdentifier> additionalIdentifiers;

    @OneToMany(mappedBy = "offenderId")
    private List<Event> events;
}

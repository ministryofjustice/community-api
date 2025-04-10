package uk.gov.justice.digital.delius.jpa.standard.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString.Exclude;
import org.hibernate.annotations.Where;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private boolean softDeleted;

    @Column(name = "ROW_VERSION")
    private Long rowVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TITLE_ID")
    @Exclude
    private StandardReference title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GENDER_ID")
    @Exclude
    private StandardReference gender;

    @Column(name = "PREFERRED_NAME")
    private String preferredName;

    @ManyToOne()
    @JoinColumn(name = "GENDER_IDENTITY_ID")
    private StandardReference genderIdentity;

    @Column(name = "GENDER_IDENTITY_DESCRIPTION")
    private String selfDescribedGender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ETHNICITY_ID")
    @Exclude
    private StandardReference ethnicity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NATIONALITY_ID")
    @Exclude
    private StandardReference nationality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IMMIGRATION_STATUS_ID")
    @Exclude
    private StandardReference immigrationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LANGUAGE_ID")
    @Exclude
    private StandardReference language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RELIGION_ID")
    @Exclude
    private StandardReference religion;

    @Column(name = "MOST_RECENT_PRISONER_NUMBER")
    private String mostRecentPrisonerNumber;

    @Column(name = "LAST_UPDATED_DATETIME")
    private LocalDateTime lastUpdatedDateTime;

    @Column(name = "LAST_UPDATED_DATETIME_DIVERSIT")
    private LocalDateTime lastUpdatedDateTimeDiversity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECOND_NATIONALITY_ID")
    @Exclude
    private StandardReference secondNationality;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    // Only select OFFENDER_ADDRESS rows where SOFT_DELETED != 1
    @Where(clause="SOFT_DELETED != 1")
    @Exclude
    private List<OffenderAddress> offenderAddresses;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    @Exclude
    private List<OffenderAlias> offenderAliases;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    @Exclude
    private List<Disability> disabilities;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    @Exclude
    private List<Provision> provisions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEXUAL_ORIENTATION_ID")
    @Exclude
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

    @Column(name = "CURRENT_REMAND_STATUS")
    private String currentRemandStatus;

    @Column(name = "DYNAMIC_RSR_SCORE")
    private Double dynamicRsrScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTITION_AREA_ID")
    @Exclude
    private PartitionArea partitionArea;

    @OneToMany(mappedBy = "offenderId")
    // Only select OFFENDER_MANAGER rows where the ACTIVE_FLAG = 1 AND SOFT_DELETED != 1
    @Where(clause = "ACTIVE_FLAG = 1 AND SOFT_DELETED != 1")
    @Exclude
    private List<OffenderManager> offenderManagers;

    @OneToMany(mappedBy = "offenderId")
    // Only select PRISON_OFFENDER_MANAGER rows where the ACTIVE_FLAG = 1 AND SOFT_DELETED= != 1
    @Where(clause = "ACTIVE_FLAG = 1 AND SOFT_DELETED != 1")
    @Exclude
    private List<PrisonOffenderManager> prisonOffenderManagers;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "OFFENDER_ID")
    @Exclude
    private List<AdditionalIdentifier> additionalIdentifiers;

    @OneToMany(mappedBy = "offenderId")
    @Exclude
    private List<Event> events;

    @OneToMany(mappedBy = "offenderId")
    @Where(clause = "ACTIVE_FLAG = 1 AND SOFT_DELETED != 1")
    @Exclude
    private List<Event> activeEvents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENT_TIER")
    @Exclude
    private StandardReference currentTier;

    @OneToMany
    @JoinColumn(name = "OFFENDER_ID")
    @Where(clause = "SOFT_DELETED != 1")
    @Exclude
    private List<PersonalContact> personalContacts;

    public Optional<PrisonOffenderManager> getResponsibleOfficerWhoIsPrisonOffenderManager() {
        return getActivePrisonOffenderManager()
                .filter(pom -> pom.getActiveResponsibleOfficer() != null);
    }

    public Optional<OffenderManager> getActiveCommunityOffenderManager() {
        return getOffenderManagers()
                .stream()
                .filter(OffenderManager::isActive)
                .findFirst();
    }

    public Optional<OffenderManager> getResponsibleOfficerWhoIsCommunityOffenderManager() {
        return getActiveCommunityOffenderManager()
                .filter(com -> com.getActiveResponsibleOfficer() != null);
    }

    public Optional<PrisonOffenderManager> getActivePrisonOffenderManager() {
        return getPrisonOffenderManagers()
                .stream()
                .filter(PrisonOffenderManager::isActive)
                .findFirst();
    }

    public boolean hasActiveSentence() {
        return currentDisposal == 1L;
    }
}

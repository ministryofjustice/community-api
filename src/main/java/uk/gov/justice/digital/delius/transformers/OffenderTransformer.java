package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Address;
import uk.gov.justice.digital.delius.data.api.ContactDetails;
import uk.gov.justice.digital.delius.data.api.ContactDetailsSummary;
import uk.gov.justice.digital.delius.data.api.PreviousConviction;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderLanguages;
import uk.gov.justice.digital.delius.data.api.OffenderManager;
import uk.gov.justice.digital.delius.data.api.OffenderProfile;
import uk.gov.justice.digital.delius.data.api.PhoneNumber;
import uk.gov.justice.digital.delius.data.api.Team;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAddress;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAlias;
import uk.gov.justice.digital.delius.jpa.standard.entity.PartitionArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class OffenderTransformer {

    private final ContactTransformer contactTransformer;

    @Autowired
    public OffenderTransformer(ContactTransformer contactTransformer) {
        this.contactTransformer = contactTransformer;
    }

    private List<PhoneNumber> phoneNumbersOf(Offender offender) {
        return ImmutableList.of(
                PhoneNumber.builder().number(Optional.ofNullable(offender.getTelephoneNumber())).type(PhoneNumber.PhoneTypes.TELEPHONE).build(),
                PhoneNumber.builder().number(Optional.ofNullable(offender.getMobileNumber())).type(PhoneNumber.PhoneTypes.MOBILE).build()
        ).stream().filter(phoneNumber -> phoneNumber.getNumber().isPresent()).collect(Collectors.toList());
    }

    private OffenderLanguages languagesOf(Offender offender) {
        return OffenderLanguages.builder()
                .primaryLanguage(Optional.ofNullable(offender.getLanguage()).map(StandardReference::getCodeDescription).orElse(null))
                .languageConcerns(Optional.ofNullable(offender.getLanguageConcerns()).orElse(null))
                .requiresInterpreter(ynToBoolean(offender.getInterpreterRequired()))
                .build();
    }

    private PreviousConviction previousConvictionOf(Offender offender) {
        return PreviousConviction.builder()
                .convictionDate(offender.getPreviousConvictionDate())
                .detail(Optional.ofNullable(offender.getPrevConvictionDocumentName()).map(doc -> ImmutableMap.of("documentName", doc)).orElse(null))
                .build();
    }

    private OffenderProfile offenderProfileOf(Offender offender) {
        return OffenderProfile.builder()
                .ethnicity(Optional.ofNullable(offender.getEthnicity()).map(StandardReference::getCodeDescription).orElse(null))
                .immigrationStatus(Optional.ofNullable(offender.getImmigrationStatus()).map(StandardReference::getCodeDescription).orElse(null))
                .nationality(Optional.ofNullable(offender.getNationality()).map(StandardReference::getCodeDescription).orElse(null))
                .offenderLanguages(languagesOf(offender))
                .previousConviction(previousConvictionOf(offender))
                .religion(Optional.ofNullable(offender.getReligion()).map(StandardReference::getCodeDescription).orElse(null))
                .remandStatus(Optional.ofNullable(offender.getCurrentRemandStatus()).orElse(null))
                .secondaryNationality(Optional.ofNullable(offender.getSecondNationality()).map(StandardReference::getCodeDescription).orElse(null))
                .sexualOrientation(Optional.ofNullable(offender.getSexualOrientation()).map(StandardReference::getCodeDescription).orElse(null))
                .riskColour(Optional.ofNullable(offender.getCurrentHighestRiskColour()).orElse(null))
                .offenderDetails(offender.getOffenderDetails())
                .build();
    }

    private IDs idsOf(Offender offender) {
        return IDs.builder()
                .crn(offender.getCrn())
                .croNumber(offender.getCroNumber())
                .immigrationNumber(offender.getImmigrationNumber())
                .niNumber(offender.getNiNumber())
                .nomsNumber(offender.getNomsNumber())
                .pncNumber(offender.getPncNumber())
                .mostRecentPrisonerNumber(offender.getMostRecentPrisonerNumber())
                .build();
    }

    private Address addressOf(OffenderAddress address) {
        return Address.builder()
                .addressNumber(address.getAddressNumber())
                .buildingName(address.getBuildingName())
                .streetName(address.getStreetName())
                .district(address.getDistrict())
                .town(address.getTownCity())
                .county(address.getCounty())
                .postcode(address.getPostcode())
                .telephoneNumber(address.getTelephoneNumber())
                .notes(address.getNotes())
                .noFixedAbode(ynToBoolean(address.getNoFixedAbode()))
                .from(address.getStartDate())
                .to(address.getEndDate())
                .status(Optional.ofNullable(address.getAddressStatus()).map(status ->
                            KeyValue.builder()
                                .code(status.getCodeValue())
                                .description(status.getCodeDescription()).build())
                        .orElse(null))
                .build();
    }

    private ContactDetails contactDetailsOf(Offender offender) {
        return ContactDetails.builder()
                .allowSMS(ynToBoolean(offender.getAllowSMS()))
                .emailAddresses(emailAddressesOf(offender))
                .phoneNumbers(phoneNumbersOf(offender))
                .addresses(addressesOf(offender))
                .build();
    }

    private ContactDetailsSummary contactDetailsSummaryOf(Offender offender) {
        return ContactDetailsSummary.builder()
                .allowSMS(ynToBoolean(offender.getAllowSMS()))
                .emailAddresses(emailAddressesOf(offender))
                .phoneNumbers(phoneNumbersOf(offender))
                .build();
    }

    private List<String> emailAddressesOf(Offender offender) {
        return Optional.ofNullable(offender.getEmailAddress()).map(Arrays::asList).orElse(Collections.emptyList());
    }

    private List<Address> addressesOf(Offender offender) {
        return offender.getOffenderAddresses().stream().map(
                this::addressOf).collect(Collectors.toList());
    }

    private uk.gov.justice.digital.delius.data.api.OffenderAlias aliasOf(OffenderAlias alias) {
        return uk.gov.justice.digital.delius.data.api.OffenderAlias.builder()
                .dateOfBirth(alias.getDateOfBirth())
                .firstName(alias.getFirstName())
                .middleNames(combinedMiddleNamesOf(alias.getSecondName(), alias.getThirdName()))
                .surname(alias.getSurname())
                .gender(Optional.ofNullable(alias.getGender()).map(StandardReference::getCodeDescription).orElse(null))
                .build();
    }

    private List<uk.gov.justice.digital.delius.data.api.OffenderAlias> offenderAliasesOf(List<OffenderAlias> offenderAliases) {
        return offenderAliases.stream().map(this::aliasOf).collect(Collectors.toList());

    }

    public OffenderDetail fullOffenderOf(Offender offender) {
        return OffenderDetail.builder()
                .offenderId(offender.getOffenderId())
                .dateOfBirth(offender.getDateOfBirthDate())
                .firstName(offender.getFirstName())
                .gender(offender.getGender().getCodeDescription())
                .middleNames(combinedMiddleNamesOf(offender.getSecondName(), offender.getThirdName()))
                .surname(offender.getSurname())
                .previousSurname(offender.getPreviousSurname())
                .title(Optional.ofNullable(offender.getTitle()).map(StandardReference::getCodeDescription).orElse(null))
                .contactDetails(contactDetailsOf(offender))
                .otherIds(idsOf(offender))
                .offenderProfile(offenderProfileOf(offender))
                .offenderAliases(offenderAliasesOf(offender.getOffenderAliases()))
                .softDeleted(zeroOneToBoolean(offender.getSoftDeleted()))
                .currentDisposal(Optional.ofNullable(offender.getCurrentDisposal()).map(Object::toString).orElse(null))
                .partitionArea(Optional.ofNullable(offender.getPartitionArea()).map(PartitionArea::getArea).orElse(null))
                .currentExclusion(zeroOneToBoolean(offender.getCurrentExclusion()))
                .currentRestriction(zeroOneToBoolean(offender.getCurrentRestriction()))
                .offenderManagers(offenderManagersOf(offender.getOffenderManagers()))
                .build();
    }

    public OffenderDetailSummary offenderSummaryOf(Offender offender) {
        return OffenderDetailSummary.builder()
                .offenderId(offender.getOffenderId())
                .dateOfBirth(offender.getDateOfBirthDate())
                .firstName(offender.getFirstName())
                .gender(offender.getGender().getCodeDescription())
                .middleNames(combinedMiddleNamesOf(offender.getSecondName(), offender.getThirdName()))
                .surname(offender.getSurname())
                .previousSurname(offender.getPreviousSurname())
                .title(Optional.ofNullable(offender.getTitle()).map(StandardReference::getCodeDescription).orElse(null))
                .contactDetails(contactDetailsSummaryOf(offender))
                .otherIds(idsOf(offender))
                .offenderProfile(offenderProfileOf(offender))
                .softDeleted(zeroOneToBoolean(offender.getSoftDeleted()))
                .currentDisposal(Optional.ofNullable(offender.getCurrentDisposal()).map(Object::toString).orElse(null))
                .partitionArea(Optional.ofNullable(offender.getPartitionArea()).map(PartitionArea::getArea).orElse(null))
                .currentExclusion(zeroOneToBoolean(offender.getCurrentExclusion()))
                .currentRestriction(zeroOneToBoolean(offender.getCurrentRestriction()))
                .build();
    }

    private List<String> combinedMiddleNamesOf(String secondName, String thirdName) {
        Optional<String> maybeSecondName = Optional.ofNullable(secondName);
        Optional<String> maybeThirdName = Optional.ofNullable(thirdName);

        return ImmutableList.of(maybeSecondName, maybeThirdName)
                .stream()
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());
    }

    public List<OffenderManager> offenderManagersOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager> offenderManagers) {
        return offenderManagers.stream()
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager::getAllocationDate)
                        .reversed())
                .map(this::offenderManagerOf).collect(Collectors.toList());
    }

    private OffenderManager offenderManagerOf(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager offenderManager) {
        return OffenderManager.builder()
                .partitionArea(partitionAreaOf(offenderManager))
                .softDeleted(zeroOneToBoolean(offenderManager.getSoftDeleted()))
                .trustOfficer(Optional.ofNullable(offenderManager.getOfficer())
                        .map(o -> humanOf(o.getForename(), o.getForename2(), o.getSurname()))
                        .orElse(null))
                .staff(contactTransformer.staffOf(offenderManager.getStaff()))
                .providerEmployee(contactTransformer.providerEmployeeOf(offenderManager.getProviderEmployee()))
                .team(teamOf(offenderManager))
                .probationArea(probationAreaOf(offenderManager.getProbationArea()))
                .active(zeroOneToBoolean(offenderManager.getActiveFlag()))
                .fromDate(localDateOf(offenderManager.getAllocationDate()))
                .toDate(localDateOf(offenderManager.getEndDate()))
                .build();
    }

    private LocalDate localDateOf(Timestamp timestamp) {
        return Optional.ofNullable(timestamp).map(t -> t.toLocalDateTime().toLocalDate()).orElse(null);
    }

    private KeyValue probationAreaOf(ProbationArea probationArea) {
        return KeyValue.builder()
                .code(probationArea.getCode())
                .description(probationArea.getDescription())
                .build();
    }

    private Team teamOf(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager offenderManager) {
        return Optional.ofNullable(offenderManager.getTrustProviderTeam())
                .map(tpt -> Team.builder()
                        .description(tpt.getDescription())
                        .telephone(tpt.getTelephone())
                        .district(Optional.ofNullable(tpt.getDistrict()).map(
                                d -> KeyValue.builder()
                                        .code(d.getCode())
                                        .description(d.getDescription()).build())
                                .orElse(null))
                        .borough(Optional.ofNullable(tpt.getDistrict()).flatMap(
                                d -> Optional.ofNullable(d.getBorough())
                                        .map(b -> KeyValue.builder()
                                                .code(b.getCode())
                                                .description(b.getDescription())
                                                .build()))
                                .orElse(null))
                        .build())
                .orElse(null);
    }

    private String partitionAreaOf(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager offenderManager) {
        return Optional.ofNullable(offenderManager.getPartitionArea())
                .map(PartitionArea::getArea)
                .orElse(null);
    }

    private Human humanOf(String forename, String forename2, String surname) {
        return Human.builder()
                .surname(surname)
                .forenames(combinedMiddleNamesOf(forename, forename2).stream().collect(Collectors.joining(" ")))
                .build();
    }
}

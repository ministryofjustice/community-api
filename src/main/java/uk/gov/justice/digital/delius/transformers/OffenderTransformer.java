package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.gov.justice.digital.delius.data.api.AdditionalIdentifier;
import uk.gov.justice.digital.delius.data.api.Address;
import uk.gov.justice.digital.delius.data.api.ContactDetails;
import uk.gov.justice.digital.delius.data.api.ContactDetailsSummary;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderLanguages;
import uk.gov.justice.digital.delius.data.api.OffenderManager;
import uk.gov.justice.digital.delius.data.api.OffenderProfile;
import uk.gov.justice.digital.delius.data.api.PhoneNumber;
import uk.gov.justice.digital.delius.data.api.PreviousConviction;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficer;
import uk.gov.justice.digital.delius.data.api.StaffCaseloadEntry;
import uk.gov.justice.digital.delius.data.api.Team;
import uk.gov.justice.digital.delius.jpa.standard.entity.AddressAssessment;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disability;
import uk.gov.justice.digital.delius.jpa.standard.entity.District;
import uk.gov.justice.digital.delius.jpa.standard.entity.Document;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAddress;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAlias;
import uk.gov.justice.digital.delius.jpa.standard.entity.PartitionArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderTeam;
import uk.gov.justice.digital.delius.jpa.standard.entity.Provision;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

public class OffenderTransformer {

    private static List<PhoneNumber> phoneNumbersOf(Offender offender) {
        return ImmutableList.of(
            PhoneNumber.builder().number(Optional.ofNullable(offender.getTelephoneNumber())).type(PhoneNumber.PhoneTypes.TELEPHONE).build(),
            PhoneNumber.builder().number(Optional.ofNullable(offender.getMobileNumber())).type(PhoneNumber.PhoneTypes.MOBILE).build()
        ).stream().filter(phoneNumber -> phoneNumber.getNumber().isPresent()).collect(toList());
    }

    private static OffenderLanguages languagesOf(Offender offender) {
        return OffenderLanguages.builder()
            .primaryLanguage(Optional.ofNullable(offender.getLanguage()).map(StandardReference::getCodeDescription).orElse(null))
            .languageConcerns(offender.getLanguageConcerns())
            .requiresInterpreter(ynToBoolean(offender.getInterpreterRequired()))
            .build();
    }

    private static PreviousConviction previousConvictionOf(Document document) {
        if (document == null) {
            return null;
        }
        return PreviousConviction.builder()
            .convictionDate(document.getCreatedDate().toLocalDate())
            .detail(Optional.ofNullable(document.getDocumentName()).map(doc -> ImmutableMap.of("documentName", doc)).orElse(null))
            .build();
    }

    private static OffenderProfile offenderProfileOf(Offender offender, Document document) {
        return OffenderProfile.builder()
            .ethnicity(Optional.ofNullable(offender.getEthnicity()).map(StandardReference::getCodeDescription).orElse(null))
            .immigrationStatus(Optional.ofNullable(offender.getImmigrationStatus()).map(StandardReference::getCodeDescription).orElse(null))
            .nationality(Optional.ofNullable(offender.getNationality()).map(StandardReference::getCodeDescription).orElse(null))
            .offenderLanguages(languagesOf(offender))
            .previousConviction(previousConvictionOf(document))
            .religion(Optional.ofNullable(offender.getReligion()).map(StandardReference::getCodeDescription).orElse(null))
            .remandStatus(offender.getCurrentRemandStatus())
            .secondaryNationality(Optional.ofNullable(offender.getSecondNationality()).map(StandardReference::getCodeDescription).orElse(null))
            .sexualOrientation(Optional.ofNullable(offender.getSexualOrientation()).map(StandardReference::getCodeDescription).orElse(null))
            .riskColour(offender.getCurrentHighestRiskColour())
            .offenderDetails(offender.getOffenderDetails())
            .disabilities(disabilitiesOf(offender.getDisabilities(), LocalDate.now()))
            .genderIdentity(Optional.ofNullable(offender.getGenderIdentity()).map(StandardReference::getCodeDescription).orElse(null))
            .selfDescribedGender(offender.getSelfDescribedGender())
            .build();
    }

    public static IDs idsOf(Offender offender) {
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

    private static Address addressOf(OffenderAddress address) {
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
            .type(Optional.ofNullable(address.getAddressType())
                .map(at -> KeyValue.builder().code(at.getCodeValue()).description(at.getCodeDescription()).build())
                .orElse(null)
            )
            .typeVerified(Optional.ofNullable(address.getTypeVerified()).map(v -> v.equals("Y")).orElse(false))
            .latestAssessmentDate(Optional.ofNullable(address.getAddressAssessments())
                .flatMap(assessments -> assessments.stream()
                    .map(AddressAssessment::getAssessmentDate)
                    .max(Comparator.naturalOrder()))
                .orElse(null))
            .createdDatetime(address.getCreatedDatetime())
            .lastUpdatedDatetime(address.getLastUpdatedDatetime())
            .build();
    }

    private static ContactDetails contactDetailsOf(Offender offender) {
        return ContactDetails.builder()
            .allowSMS(ynToBoolean(offender.getAllowSMS()))
            .emailAddresses(OffenderTransformer.emailAddressesOf(offender))
            .phoneNumbers(phoneNumbersOf(offender))
            .addresses(OffenderTransformer.addressesOf(offender))
            .build();
    }

    private static ContactDetailsSummary contactDetailsSummaryOf(Offender offender) {
        return ContactDetailsSummary.builder()
            .allowSMS(ynToBoolean(offender.getAllowSMS()))
            .emailAddresses(OffenderTransformer.emailAddressesOf(offender))
            .phoneNumbers(phoneNumbersOf(offender))
            .build();
    }

    private static List<String> emailAddressesOf(Offender offender) {
        return Optional.ofNullable(offender.getEmailAddress()).map(Arrays::asList).orElse(Collections.emptyList());
    }

    private static List<Address> addressesOf(Offender offender) {
        return offender.getOffenderAddresses().stream().map(
            OffenderTransformer::addressOf).collect(toList());
    }

    private static uk.gov.justice.digital.delius.data.api.OffenderAlias aliasOf(OffenderAlias alias) {
        return uk.gov.justice.digital.delius.data.api.OffenderAlias.builder()
            .id(String.valueOf(alias.getAliasID()))
            .dateOfBirth(alias.getDateOfBirth())
            .firstName(alias.getFirstName())
            .middleNames(combinedMiddleNamesOf(alias.getSecondName(), alias.getThirdName()))
            .surname(alias.getSurname())
            .gender(Optional.ofNullable(alias.getGender()).map(StandardReference::getCodeDescription).orElse(null))
            .build();
    }

    private static List<uk.gov.justice.digital.delius.data.api.OffenderAlias> offenderAliasesOf(List<OffenderAlias> offenderAliases) {
        return offenderAliases.stream().map(OffenderTransformer::aliasOf).collect(toList());

    }

    public static OffenderDetail fullOffenderOf(Offender offender, Document document) {
        return OffenderDetail.builder()
            .offenderId(offender.getOffenderId())
            .dateOfBirth(offender.getDateOfBirthDate())
            .firstName(offender.getFirstName())
            .gender(offender.getGender().getCodeDescription())
            .middleNames(combinedMiddleNamesOf(offender.getSecondName(), offender.getThirdName()))
            .surname(offender.getSurname())
            .preferredName(offender.getPreferredName())
            .previousSurname(offender.getPreviousSurname())
            .title(Optional.ofNullable(offender.getTitle()).map(StandardReference::getCodeDescription).orElse(null))
            .contactDetails(contactDetailsOf(offender))
            .otherIds(idsOf(offender))
            .offenderProfile(offenderProfileOf(offender, document))
            .offenderAliases(offenderAliasesOf(offender.getOffenderAliases()))
            .softDeleted(offender.isSoftDeleted())
            .currentDisposal(Optional.ofNullable(offender.getCurrentDisposal()).map(Object::toString).orElse(null))
            .partitionArea(Optional.ofNullable(offender.getPartitionArea()).map(PartitionArea::getArea).orElse(null))
            .currentExclusion(zeroOneToBoolean(offender.getCurrentExclusion()))
            .exclusionMessage(offender.getExclusionMessage())
            .currentRestriction(zeroOneToBoolean(offender.getCurrentRestriction()))
            .restrictionMessage(offender.getRestrictionMessage())
            .offenderManagers(OffenderTransformer.offenderManagersOf(offender.getOffenderManagers()))
            .currentTier(Optional.ofNullable(offender.getCurrentTier()).map(StandardReference::getCodeDescription).orElse(null))
            .build();
    }

    public static OffenderDetailSummary offenderSummaryOf(Offender offender, Document document) {
        return OffenderDetailSummary.builder()
            .offenderId(offender.getOffenderId())
            .dateOfBirth(offender.getDateOfBirthDate())
            .firstName(offender.getFirstName())
            .gender(offender.getGender().getCodeDescription())
            .middleNames(combinedMiddleNamesOf(offender.getSecondName(), offender.getThirdName()))
            .surname(offender.getSurname())
            .previousSurname(offender.getPreviousSurname())
            .preferredName(offender.getPreferredName())
            .title(Optional.ofNullable(offender.getTitle()).map(StandardReference::getCodeDescription).orElse(null))
            .contactDetails(contactDetailsSummaryOf(offender))
            .otherIds(idsOf(offender))
            .offenderProfile(offenderProfileOf(offender, document))
            .softDeleted(offender.isSoftDeleted())
            .currentDisposal(Optional.ofNullable(offender.getCurrentDisposal()).map(Object::toString).orElse(null))
            .partitionArea(Optional.ofNullable(offender.getPartitionArea()).map(PartitionArea::getArea).orElse(null))
            .currentExclusion(zeroOneToBoolean(offender.getCurrentExclusion()))
            .currentRestriction(zeroOneToBoolean(offender.getCurrentRestriction()))
            .build();
    }

    private static List<String> combinedMiddleNamesOf(String secondName, String thirdName) {
        Optional<String> maybeSecondName = Optional.ofNullable(secondName);
        Optional<String> maybeThirdName = Optional.ofNullable(thirdName);

        return ImmutableList.of(maybeSecondName, maybeThirdName)
            .stream()
            .flatMap(Optional::stream)
            .collect(toList());
    }

    public static List<OffenderManager> offenderManagersOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager> offenderManagers) {
        return offenderManagers.stream()
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager::getAllocationDate)
                .reversed())
            .map(OffenderTransformer::offenderManagerOf).collect(toList());
    }

    private static OffenderManager offenderManagerOf(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager offenderManager) {
        return OffenderManager.builder()
            .partitionArea(partitionAreaOf(offenderManager))
            .softDeleted(zeroOneToBoolean(offenderManager.getSoftDeleted()))
            .trustOfficer(Optional.ofNullable(offenderManager.getOfficer())
                .map(o -> humanOf(o.getForename(), o.getForename2(), o.getSurname()))
                .orElse(null))
            .staff(ContactTransformer.staffOf(offenderManager.getStaff()))
            .providerEmployee(ContactTransformer.providerEmployeeOf(offenderManager.getProviderEmployee()))
            .team(teamOf(offenderManager))
            .probationArea(probationAreaOf(offenderManager.getProbationArea()))
            .active(zeroOneToBoolean(offenderManager.getActiveFlag()))
            .fromDate(offenderManager.getAllocationDate())
            .toDate(offenderManager.getEndDate())
            .allocationReason(Optional.ofNullable(offenderManager.getAllocationReason())
                .map(OffenderTransformer::allocationReasonOf)
                .orElse(null))
            .build();
    }

    private static KeyValue allocationReasonOf(StandardReference allocationReason) {
        return KeyValue
            .builder()
            .code(allocationReason.getCodeValue())
            .description(allocationReason.getCodeDescription())
            .build();
    }

    private static uk.gov.justice.digital.delius.data.api.ProbationArea probationAreaOf(ProbationArea probationArea) {
        return uk.gov.justice.digital.delius.data.api.ProbationArea.builder()
            .code(probationArea.getCode())
            .description(probationArea.getDescription())
            .nps(npsZeroOneToBoolean(probationArea.getPrivateSector()))
            .build();
    }

    private static Boolean npsZeroOneToBoolean(Long zeroOrOne) {
        return Optional.ofNullable(zeroOrOne).map(value -> value == 0).orElse(null);
    }

    private static Team teamOf(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager offenderManager) {
        /*
         * This only populates a subset of team data - this is currently indexed in elasticsearch so if this is modified
         * then it will lead to inconsistent data and the index will need to be rebuilt.
         */
        return Optional.ofNullable(offenderManager.getTeam())
            .map(tpt -> Team.builder()
                .code(tpt.getCode())
                .description(tpt.getDescription())
                .telephone(tpt.getTelephone())
                .district(Optional.ofNullable(tpt.getDistrict()).map(
                        d -> KeyValue.builder()
                            .code(d.getCode())
                            .description(d.getDescription()).build())
                    .orElse(null))
                .localDeliveryUnit(keyValueOf(tpt.getDistrict()))
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

    private static KeyValue keyValueOf(District district) {
        return KeyValue
            .builder()
            .code(district.getCode())
            .description(district.getDescription())
            .build();
    }

    private static String partitionAreaOf(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager offenderManager) {
        return Optional.ofNullable(offenderManager.getPartitionArea())
            .map(PartitionArea::getArea)
            .orElse(null);
    }

    private static Human humanOf(String forename, String forename2, String surname) {
        return Human.builder()
            .surname(surname)
            .forenames(String.join(" ", OffenderTransformer.combinedMiddleNamesOf(forename, forename2)))
            .build();
    }

    private static List<uk.gov.justice.digital.delius.data.api.Disability> disabilitiesOf(List<Disability> disabilities, LocalDate dateToCompare) {
        return disabilities.stream()
            .filter(disability -> disability.getSoftDeleted() == 0)
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Disability::getStartDate)
                .reversed())
            .map(disability -> disabilityOf(disability, dateToCompare)).collect(toList());
    }

    private static uk.gov.justice.digital.delius.data.api.Disability disabilityOf(Disability disability, LocalDate dateToCompare) {
        return uk.gov.justice.digital.delius.data.api.Disability
            .builder()
            .disabilityId(disability.getDisabilityId())
            .endDate(disability.getFinishDate())
            .notes(disability.getNotes())
            .startDate(disability.getStartDate())
            .disabilityType(KeyValue
                .builder()
                .code(disability.getDisabilityType().getCodeValue())
                .description(disability.getDisabilityType().getCodeDescription()).build())
            .provisions(provisionsOf(disability.getProvisions()))
            .lastUpdatedDateTime(disability.getLastUpdatedDatetime())
            .isActive(isActiveOf(disability, dateToCompare))
            .build();
    }

    private static List<uk.gov.justice.digital.delius.data.api.Provision> provisionsOf(List<Provision> disabilityProvisions) {
        return Optional.ofNullable(disabilityProvisions)
            .map(provisions -> provisions
                .stream()
                .filter(provision -> provision.getSoftDeleted() == 0)
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Provision::getStartDate)
                    .reversed())
                .map(OffenderTransformer::provisionOf).collect(toList())
            )
            .orElse(List.of());
    }

    private static uk.gov.justice.digital.delius.data.api.Provision provisionOf(Provision provision) {
        return uk.gov.justice.digital.delius.data.api.Provision
            .builder()
            .provisionId(provision.getProvisionID())
            .notes(provision.getNotes())
            .startDate(provision.getStartDate())
            .finishDate(provision.getFinishDate())
            .provisionType(KeyValue.builder().code(provision.getProvisionType().getCodeValue())
                .description(provision.getProvisionType().getCodeDescription()).build())
            .build();
    }

    private static Boolean isActiveOf(uk.gov.justice.digital.delius.jpa.standard.entity.Disability disability, LocalDate dateToCompare) {
        if (disability.getStartDate().isAfter(dateToCompare)) {
            return false;
        }
        return Optional.ofNullable(disability.getFinishDate()).map(
            end -> disability.getFinishDate().isAfter(dateToCompare)
        ).orElse(true);
    }

    private static ResponsibleOfficer convertToResponsibleOfficer(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager om, Offender offender) {

        return ResponsibleOfficer
            .builder()
            .nomsNumber(offender.getNomsNumber())
            .responsibleOfficerId(Objects.nonNull(om.getActiveResponsibleOfficer()) ? om.getActiveResponsibleOfficer().getResponsibleOfficerId() : null)
            .offenderManagerId(om.getOffenderManagerId())
            .prisonOffenderManagerId(null)
            .staffCode(Optional.ofNullable(om.getStaff()).map(Staff::getOfficerCode).orElse(null))
            .surname(Optional.ofNullable(om.getStaff()).map(Staff::getSurname).orElse(null))
            .forenames(String.join(" ", OffenderTransformer
                .combinedMiddleNamesOf(om.getStaff().getForename(), om.getStaff().getForname2())))
            .providerTeamCode(Optional.ofNullable(om.getProviderTeam()).map(ProviderTeam::getCode).orElse(null))
            .providerTeamDescription(Optional.ofNullable(om.getProviderTeam()).map(ProviderTeam::getName).orElse(null))
            .lduCode(Optional.ofNullable(om.getTeam()).map(team -> team.getLocalDeliveryUnit().getCode()).orElse(null))
            .lduDescription(Optional.ofNullable(om.getTeam()).map(team -> team.getLocalDeliveryUnit().getDescription()).orElse(null))
            .probationAreaCode(Optional.ofNullable(om.getProbationArea()).map(ProbationArea::getCode).orElse(null))
            .probationAreaDescription(Optional.ofNullable(om.getProbationArea()).map(ProbationArea::getDescription).orElse(null))
            .isCurrentRo(Objects.nonNull(om.getActiveResponsibleOfficer()))
            .isCurrentOm(isCurrentManager(om.getActiveFlag(), om.getEndDate()))
            .isCurrentPom(false)
            .omStartDate(om.getAllocationDate())
            .omEndDate(om.getEndDate())
            .build();
    }

    private static ResponsibleOfficer convertToResponsibleOfficer(uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager pom, Offender offender) {

        return ResponsibleOfficer
            .builder()
            .nomsNumber(offender.getNomsNumber())
            .responsibleOfficerId((Objects.nonNull(pom.getActiveResponsibleOfficer()) ? pom.getActiveResponsibleOfficer().getResponsibleOfficerId() : null))
            .prisonOffenderManagerId(pom.getPrisonOffenderManagerId())
            .staffCode(Optional.ofNullable(pom.getStaff()).map(Staff::getOfficerCode).orElse(null))
            .surname(Optional.ofNullable(pom.getStaff()).map(Staff::getSurname).orElse(null))
            .forenames(String.join(" ", OffenderTransformer
                .combinedMiddleNamesOf(pom.getStaff().getForename(), pom.getStaff().getForname2())))
            .providerTeamCode(Optional.ofNullable(pom.getTeam()).map(uk.gov.justice.digital.delius.jpa.standard.entity.Team::getCode).orElse(null))
            .providerTeamDescription(Optional.ofNullable(pom.getTeam()).map(uk.gov.justice.digital.delius.jpa.standard.entity.Team::getDescription).orElse(null))
            .lduCode(Optional.ofNullable(pom.getTeam()).map(team -> team.getLocalDeliveryUnit().getCode()).orElse(null))
            .lduDescription(Optional.ofNullable(pom.getTeam()).map(team -> team.getLocalDeliveryUnit().getDescription()).orElse(null))
            .probationAreaCode(Optional.ofNullable(pom.getProbationArea()).map(ProbationArea::getCode).orElse(null))
            .probationAreaDescription(Optional.ofNullable(pom.getProbationArea()).map(ProbationArea::getDescription).orElse(null))
            .isCurrentRo(Objects.nonNull(pom.getActiveResponsibleOfficer()))
            .isCurrentOm(false)
            .isCurrentPom(isCurrentManager(pom.getActiveFlag(), pom.getEndDate()))
            .omStartDate(null)
            .omEndDate(null)
            .build();
    }

    public static List<uk.gov.justice.digital.delius.data.api.ResponsibleOfficer> responsibleOfficersOf(Offender offender, boolean current) {

        List<ResponsibleOfficer> responsibleOfficers = new ArrayList<>();

        if (offender.getOffenderManagers() != null && !offender.getOffenderManagers().isEmpty()) {
            responsibleOfficers.addAll(
                offender.getOffenderManagers().stream()
                    .map(offMgr -> OffenderTransformer.convertToResponsibleOfficer(offMgr, offender)).toList());
        }

        if (offender.getPrisonOffenderManagers() != null && !offender.getPrisonOffenderManagers().isEmpty()) {
            responsibleOfficers.addAll(
                offender.getPrisonOffenderManagers().stream()
                    .map(prisonOffenderManager -> OffenderTransformer
                        .convertToResponsibleOfficer(prisonOffenderManager, offender)).toList());
        }

        if (current && !responsibleOfficers.isEmpty()) {
            // Filter the list to only those officers who hold a currently active role for this offender
            return responsibleOfficers
                .stream()
                .filter(ro -> (ro.isCurrentOm() || ro.isCurrentPom() || ro.isCurrentRo()))
                .collect(toList());
        }

        return responsibleOfficers;
    }

    private static ManagedOffender convertToManagedOffender(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager om, Staff staff) {

        return ManagedOffender.builder()
            .staffCode(staff.getOfficerCode())
            .staffIdentifier(staff.getStaffId())
            .offenderId(om.getOffenderId())
            .nomsNumber(Optional.ofNullable(om.getManagedOffender()).map(Offender::getNomsNumber).orElse(null))
            .crnNumber(Optional.ofNullable(om.getManagedOffender()).map(Offender::getCrn).orElse(null))
            .offenderSurname(Optional.ofNullable(om.getManagedOffender()).map(Offender::getSurname).orElse(null))
            .isCurrentRo(Objects.nonNull(om.getActiveResponsibleOfficer()))
            .isCurrentOm(isCurrentManager(om.getActiveFlag(), om.getEndDate()))
            .isCurrentPom(false)
            .omStartDate(om.getAllocationDate())
            .omEndDate(om.getEndDate())
            .build();
    }

    private static ManagedOffender convertToManagedOffender(uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager pom, Staff staff) {

        return ManagedOffender.builder()
            .staffCode(staff.getOfficerCode())
            .staffIdentifier(staff.getStaffId())
            .offenderId(pom.getOffenderId())
            .nomsNumber(Optional.ofNullable(pom.getManagedOffender()).map(Offender::getNomsNumber).orElse(null))
            .crnNumber(Optional.ofNullable(pom.getManagedOffender()).map(Offender::getCrn).orElse(null))
            .offenderSurname(Optional.ofNullable(pom.getManagedOffender()).map(Offender::getSurname).orElse(null))
            .isCurrentRo(Objects.nonNull(pom.getActiveResponsibleOfficer()))
            .isCurrentOm(false)
            .isCurrentPom(isCurrentManager(pom.getActiveFlag(), pom.getEndDate()))
            .omStartDate(null)
            .omEndDate(null)
            .build();
    }

    public static List<uk.gov.justice.digital.delius.data.api.ManagedOffender> managedOffenderOf(Staff staff, boolean current) {

        List<ManagedOffender> managedOffenders = new ArrayList<>();

        if (staff.getOffenderManagers() != null && !staff.getOffenderManagers().isEmpty()) {
            managedOffenders.addAll(
                staff.getOffenderManagers().stream()
                    .map(offenderManager -> convertToManagedOffender(offenderManager, staff)).toList());
        }

        if (staff.getPrisonOffenderManagers() != null && !staff.getPrisonOffenderManagers().isEmpty()) {
            managedOffenders.addAll(
                staff.getPrisonOffenderManagers().stream()
                    .map(prisonOffenderManager -> convertToManagedOffender(prisonOffenderManager, staff)).toList());
        }

        if (current && !managedOffenders.isEmpty()) {

            // Filter the list of managed offenders to only those which hold currently active OM, POM or RO roles
            return managedOffenders
                .stream()
                .filter(mo -> (mo.isCurrentOm() || mo.isCurrentPom() || mo.isCurrentRo()))
                .collect(toList());
        }

        return managedOffenders;
    }

    public static List<AdditionalIdentifier> additionalIdentifiersOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalIdentifier> additionalIdentifiers) {
        return Optional.ofNullable(additionalIdentifiers)
            .map(identifiers -> identifiers
                .stream()
                .filter(not(uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalIdentifier::isDeleted))
                .map(additionalIdentifier -> AdditionalIdentifier
                    .builder()
                    .additionalIdentifierId(additionalIdentifier.getAdditionalIdentifierId())
                    .value(additionalIdentifier.getIdentifier())
                    .type(KeyValue
                        .builder()
                        .code(additionalIdentifier.getIdentifierName().getCodeValue())
                        .description(additionalIdentifier.getIdentifierName().getCodeDescription())
                        .build())
                    .build())
                .collect(toList()))
            .orElse(List.of());
    }

    public static StaffCaseloadEntry caseOf(final Offender offender) {
        return StaffCaseloadEntry.builder().crn(offender.getCrn()).firstName(offender.getFirstName())
            .middleNames(combinedMiddleNamesOf(offender.getSecondName(), offender.getThirdName()))
            .surname(offender.getSurname())
            .preferredName(offender.getPreferredName())
            .build();
    }

    private static boolean isCurrentManager(Long activeFlag, LocalDate endDate) {
        boolean result = activeFlag.intValue() == 1 && endDate == null;
        return result;
    }

}

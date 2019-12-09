package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.OffenderManager;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficer;
import uk.gov.justice.digital.delius.data.api.Team;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disability;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAlias;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
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
                .disabilities(disabilitiesOf(offender.getDisabilities()))
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
                .exclusionMessage(offender.getExclusionMessage())
                .currentRestriction(zeroOneToBoolean(offender.getCurrentRestriction()))
                .restrictionMessage(offender.getRestrictionMessage())
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
                .fromDate(offenderManager.getAllocationDate())
                .toDate(offenderManager.getEndDate())
                .allocationReason(Optional.ofNullable(offenderManager.getAllocationReason())
                        .map(this::allocationReasonOf)
                        .orElse(null))
                .build();
    }

    private KeyValue allocationReasonOf(StandardReference allocationReason) {
        return KeyValue
                .builder()
                .code(allocationReason.getCodeValue())
                .description(allocationReason.getCodeDescription())
                .build();
    }

    private uk.gov.justice.digital.delius.data.api.ProbationArea probationAreaOf(ProbationArea probationArea) {
        return uk.gov.justice.digital.delius.data.api.ProbationArea.builder()
                .code(probationArea.getCode())
                .description(probationArea.getDescription())
                .nps(npsZeroOneToBoolean(probationArea.getPrivateSector()))
                .build();
    }

    private static Boolean npsZeroOneToBoolean(Long zeroOrOne) {
        return Optional.ofNullable(zeroOrOne).map(value -> value == 0).orElse(null);
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

    private List<uk.gov.justice.digital.delius.data.api.Disability> disabilitiesOf(List<Disability> disabilities) {
        return disabilities.stream()
                .filter(disability -> disability.getSoftDeleted() == 0)
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Disability::getStartDate)
                        .reversed())
                .map(this::disabilityOf).collect(Collectors.toList());
    }

    private uk.gov.justice.digital.delius.data.api.Disability disabilityOf(Disability disability) {
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
                .build();
    }

    private ResponsibleOfficer convertToResponsibleOfficer(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager om, Offender offender) {

       ResponsibleOfficer ro = uk.gov.justice.digital.delius.data.api.ResponsibleOfficer
                            .builder()
                            .nomsNumber(offender.getNomsNumber())
                            .responsibleOfficerId(isCurrentRo(om.getResponsibleOfficer()) ? om.getResponsibleOfficer().getResponsibleOfficerId() : null)
                            .offenderManagerId(om.getOffenderManagerId())
                            .prisonOffenderManagerId(null)
                            .staffCode(Optional.ofNullable(om.getStaff()).map(staff -> staff.getOfficerCode()).orElse(null))
                            .surname(Optional.ofNullable(om.getStaff()).map(staff -> staff.getSurname()).orElse(null))
                            .forenames(combinedMiddleNamesOf(om.getStaff().getForename(), om.getStaff().getForname2()).stream().collect(Collectors.joining(" ")))
                            .providerTeamCode(Optional.ofNullable(om.getProviderTeam()).map(team -> team.getCode()).orElse(null))
                            .providerTeamDescription(Optional.ofNullable(om.getProviderTeam()).map(team -> team.getName()).orElse(null))
                            .lduCode(Optional.ofNullable(om.getTeam()).map(team -> team.getLocalDeliveryUnit().getCode()).orElse(null))
                            .lduDescription(Optional.ofNullable(om.getTeam()).map(team -> team.getLocalDeliveryUnit().getDescription()).orElse(null))
                            .probationAreaCode(Optional.ofNullable(om.getProbationArea()).map(pa -> pa.getCode()).orElse(null))
                            .probationAreaDescription(Optional.ofNullable(om.getProbationArea()).map(pa -> pa.getDescription()).orElse(null))
                            .isCurrentRo(isCurrentRo(om.getResponsibleOfficer()))
                            .isCurrentOm(isCurrentManager(om.getActiveFlag(), om.getEndDate()))
                            .isCurrentPom(false)
                            .omStartDate(om.getAllocationDate())
                            .omEndDate(om.getEndDate())
                            .build();

        return ro;
    }

    private ResponsibleOfficer convertToResponsibleOfficer(uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager pom, Offender offender) {

        ResponsibleOfficer ro = uk.gov.justice.digital.delius.data.api.ResponsibleOfficer
                        .builder()
                        .nomsNumber(offender.getNomsNumber())
                        .responsibleOfficerId((isCurrentRo(pom.getResponsibleOfficer()) ? pom.getResponsibleOfficer().getResponsibleOfficerId() : null))
                        .prisonOffenderManagerId(pom.getPrisonOffenderManagerId())
                        .staffCode(Optional.ofNullable(pom.getStaff()).map(staff -> staff.getOfficerCode()).orElse(null))
                        .surname(Optional.ofNullable(pom.getStaff()).map(staff -> staff.getSurname()).orElse(null))
                        .forenames(combinedMiddleNamesOf(pom.getStaff().getForename(), pom.getStaff().getForname2()).stream().collect(Collectors.joining(" ")))
                        .providerTeamCode(Optional.ofNullable(pom.getTeam()).map(team -> team.getCode()).orElse(null))
                        .providerTeamDescription(Optional.ofNullable(pom.getTeam()).map(team -> team.getDescription()).orElse(null))
                        .lduCode(Optional.ofNullable(pom.getTeam()).map(team -> team.getLocalDeliveryUnit().getCode()).orElse(null))
                        .lduDescription(Optional.ofNullable(pom.getTeam()).map(team -> team.getLocalDeliveryUnit().getDescription()).orElse(null))
                        .probationAreaCode(Optional.ofNullable(pom.getProbationArea()).map(pa -> pa.getCode()).orElse(null))
                        .probationAreaDescription(Optional.ofNullable(pom.getProbationArea()).map(pa -> pa.getDescription()).orElse(null))
                        .isCurrentRo(isCurrentRo(pom.getResponsibleOfficer()))
                        .isCurrentOm(false)
                        .isCurrentPom(isCurrentManager(pom.getActiveFlag(), pom.getEndDate()))
                        .omStartDate(null)
                        .omEndDate(null)
                        .build();

        return ro;
    }

    public List<uk.gov.justice.digital.delius.data.api.ResponsibleOfficer> responsibleOfficersOf(Offender offender, boolean current) {

        List<ResponsibleOfficer> responsibleOfficers = new ArrayList<>();

        if (offender.getOffenderManagers() != null && !offender.getOffenderManagers().isEmpty()) {
            responsibleOfficers.addAll(
                    offender.getOffenderManagers().stream()
                    .map(offMgr -> convertToResponsibleOfficer(offMgr, offender))
                    .collect(Collectors.toList()));
        }

        if (offender.getPrisonOffenderManagers() != null && !offender.getPrisonOffenderManagers().isEmpty()) {
            responsibleOfficers.addAll(
                    offender.getPrisonOffenderManagers().stream()
                    .map(prisonOffenderManager -> convertToResponsibleOfficer(prisonOffenderManager, offender))
                    .collect(Collectors.toList()));
        }

        if (current && !responsibleOfficers.isEmpty()) {
            // Filter the list to only those officers who hold a currently active role for this offender
            return responsibleOfficers
                    .stream()
                    .filter(ro -> (ro.isCurrentOm() || ro.isCurrentPom() || ro.isCurrentRo()))
                    .collect(Collectors.toList());
        }

        return responsibleOfficers;
    }

    private ManagedOffender convertToManagedOffender(uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager om, Staff staff) {

        ManagedOffender mo = uk.gov.justice.digital.delius.data.api.ManagedOffender.builder()
                .staffCode(staff.getOfficerCode())
                .offenderId(om.getOffenderId())
                .nomsNumber(Optional.ofNullable(om.getManagedOffender()).map(o -> o.getNomsNumber()).orElse(null))
                .crnNumber(Optional.ofNullable(om.getManagedOffender()).map(o -> o.getCrn()).orElse(null))
                .offenderSurname(Optional.ofNullable(om.getManagedOffender()).map(o -> o.getSurname()).orElse(null))
                .isCurrentRo(isCurrentRo(om.getResponsibleOfficer()))
                .isCurrentOm(isCurrentManager(om.getActiveFlag(), om.getEndDate()))
                .isCurrentPom(false)
                .omStartDate(om.getAllocationDate())
                .omEndDate(om.getEndDate())
                .build();

        return mo;
    }

    private ManagedOffender convertToManagedOffender(uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager pom, Staff staff) {

         ManagedOffender mo = uk.gov.justice.digital.delius.data.api.ManagedOffender.builder()
                .staffCode(staff.getOfficerCode())
                .offenderId(pom.getOffenderId())
                .nomsNumber(Optional.ofNullable(pom.getManagedOffender()).map(o -> o.getNomsNumber()).orElse(null))
                .crnNumber(Optional.ofNullable(pom.getManagedOffender()).map(o -> o.getCrn()).orElse(null))
                .offenderSurname(Optional.ofNullable(pom.getManagedOffender()).map(o -> o.getSurname()).orElse(null))
                .isCurrentRo(isCurrentRo(pom.getResponsibleOfficer()))
                .isCurrentOm(false)
                .isCurrentPom(isCurrentManager(pom.getActiveFlag(),pom.getEndDate()))
                .omStartDate(null)
                .omEndDate(null)
                .build();

        return mo;
    }

    public List<uk.gov.justice.digital.delius.data.api.ManagedOffender> managedOffenderOf(Staff staff, boolean current) {

         List<ManagedOffender> managedOffenders = new ArrayList<>();

        if (staff.getOffenderManagers() != null && !staff.getOffenderManagers().isEmpty()) {
            managedOffenders.addAll(
                    staff.getOffenderManagers().stream()
                            .map(offenderManager -> convertToManagedOffender(offenderManager, staff))
                            .collect(Collectors.toList()));
        }

        if (staff.getPrisonOffenderManagers() != null && !staff.getPrisonOffenderManagers().isEmpty()) {
            managedOffenders.addAll(
                    staff.getPrisonOffenderManagers().stream()
                    .map(prisonOffenderManager -> convertToManagedOffender(prisonOffenderManager, staff))
                    .collect(Collectors.toList()));
        }

        if (current && !managedOffenders.isEmpty()) {

            // Filter the list of managed offenders to only those which hold currently active OM, POM or RO roles
            return managedOffenders
                    .stream()
                    .filter(mo -> (mo.isCurrentOm() || mo.isCurrentPom() || mo.isCurrentRo()))
                    .collect(Collectors.toList());
        }

        return managedOffenders;
    }


    private boolean isCurrentRo(uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer ro) {
        boolean result = false;
        if (ro != null && ro.getEndDateTime() == null) {
            result = true;
        }
        return result;
    }

    private boolean isCurrentManager(Long activeFlag, LocalDate endDate) {
        boolean result = false;
        if (activeFlag.intValue() == 1 && endDate == null) {
            result = true;
        }
        return result;
    }

}

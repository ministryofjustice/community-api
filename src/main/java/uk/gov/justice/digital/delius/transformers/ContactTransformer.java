package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdRequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Explanation;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceCondition;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceConditionTypeMainCat;
import uk.gov.justice.digital.delius.jpa.standard.entity.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiType;
import uk.gov.justice.digital.delius.jpa.standard.entity.PartitionArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderEmployee;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderLocation;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderTeam;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ContactTransformer {

    public List<Contact> contactsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.Contact> contacts) {
        return contacts.stream()
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Contact::getCreatedDateTime))
                .map(this::contactOf)
                .collect(Collectors.toList());
    }

    public uk.gov.justice.digital.delius.data.api.Contact contactOf(uk.gov.justice.digital.delius.jpa.standard.entity.Contact contact) {
        return uk.gov.justice.digital.delius.data.api.Contact.builder()
                .eventId(eventIdOf(contact.getEvent()))
                .alertActive("Y".equals(contact.getAlertActive()))
                .contactEndTime(contact.getContactEndTime())
                .contactId(contact.getContactId())
                .contactOutcomeType(contactOutcomeTypeOf(contact.getContactOutcomeType()))
                .contactStartTime(contact.getContactStartTime())
                .contactType(contactTypeOf(contact.getContactType()))
                .createdDateTime(contact.getCreatedDateTime())
                .explanation(explanationOf(contact.getExplanation()))
                .lastUpdatedDateTime(contact.getLastUpdatedDateTime())
                .licenceCondition(licenceConditionOf(contact.getLicenceCondition()))
                .linkedContactId(Optional.ofNullable(contact.getLinkedContactId()))
                .notes(Optional.ofNullable(contact.getNotes()))
                .nsi(nsiOf(contact.getNsi()))
                .requirement(requirementOf(contact.getRequirement()))
                .softDeleted(contact.getSoftDeleted())
                .probationArea(probationAreaOf(contact.getProbationArea()))
                .partitionArea(partitionAreaOf(contact.getPartitionArea()))
                .providerEmployee(providerEmployeeOf(contact.getProviderEmployee()))
                .providerLocation(providerLocationOf(contact.getProviderLocation()))
                .providerTeam(providerTeamOf(contact.getProviderTeam()))
                .staff(staffOf(contact.getStaff()))
                .team(teamOf(contact.getTeam()))
                .hoursCredited(Optional.ofNullable(contact.getHoursCredited()))
                .visorContact(maybeBooleanOf(contact.getVisorContact()))
                .attended(maybeBooleanOf(contact.getAttended()))
                .complied(maybeBooleanOf(contact.getComplied()))
                .uploadLinked(maybeBooleanOf(contact.getUploadLinked()))
                .documentLinked(maybeBooleanOf(contact.getDocumentLinked()))
                .build();
    }

    private Optional<Long> eventIdOf(Event event) {
        return Optional.ofNullable(event).map(e -> e.getEventId());
    }

    private Optional<Boolean> maybeBooleanOf(String yn) {
        return Optional.ofNullable(yn).map("Y"::equals);
    }

    private Optional<KeyValue> teamOf(Team team) {
        return Optional.ofNullable(team).map(t -> KeyValue.builder().code(t.getCode()).description(t.getDescription()).build());
    }

    public Optional<Human> staffOf(Staff staff) {
        return Optional.ofNullable(staff).map(s -> Human
                .builder()
                .forenames(combinedForenamesOf(s.getForename(), s.getForname2()))
                .surname(s.getSurname())
                .build());
    }

    private Optional<KeyValue> providerTeamOf(ProviderTeam providerTeam) {
        return Optional.ofNullable(providerTeam).map(pt -> KeyValue.builder().code(pt.getCode()).description(pt.getName()).build());
    }

    private Optional<KeyValue> providerLocationOf(ProviderLocation providerLocation) {
        return Optional.ofNullable(providerLocation).map(
                pl -> KeyValue.builder().code(providerLocation.getCode()).description(providerLocation.getDescription()).build()
        );
    }

    public Optional<Human> providerEmployeeOf(ProviderEmployee providerEmployee) {
        return Optional.ofNullable(providerEmployee)
                .map(pe -> Human
                        .builder()
                        .forenames(combinedForenamesOf(pe.getForename(), pe.getForname2()))
                        .surname(pe.getSurname())
                        .build());
    }

    private String combinedForenamesOf(String name1, String name2) {
        Optional<String> maybeSecondName = Optional.ofNullable(name1);
        Optional<String> maybeThirdName = Optional.ofNullable(name2);

        return ImmutableList.of(maybeSecondName, maybeThirdName)
                .stream()
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.joining(" "));
    }

    private Optional<String> partitionAreaOf(PartitionArea partitionArea) {
        return Optional.ofNullable(partitionArea).map(PartitionArea::getArea);
    }

    private Optional<KeyValue> probationAreaOf(ProbationArea probationArea) {
        return Optional.ofNullable(probationArea).map(
                pa -> KeyValue.builder().code(pa.getCode()).description(pa.getDescription()).build()
        );
    }

    private Optional<uk.gov.justice.digital.delius.data.api.Requirement> requirementOf(Requirement requirement) {
        return Optional.ofNullable(requirement).map(req -> uk.gov.justice.digital.delius.data.api.Requirement.builder()
                .active(req.getActiveFlag() == 1)
                .adRequirementTypeMainCategory(adRequirementMainCategoryOf(req.getAdRequirementTypeMainCategory()))
                .adRequirementTypeSubCategory(adRequirementSubCategoryOf(req.getAdRequirementTypeSubCategory()))
                .commencementDate(req.getCommencementDate())
                .expectedEndDate(req.getExpectedEndDate())
                .expectedStartDate(req.getExpectedStartDate())
                .requirementId(req.getRequirementId())
                .requirementNotes(Optional.ofNullable(req.getRequirementNotes()))
                .requirementTypeMainCategory(requirementTypeMainCategoryOf(req.getRequirementTypeMainCategory()))
                .requirementTypeSubCategory(requirementTypeSubCategoryOf(req.getRequirementTypeSubCategory()))
                .startDate(req.getStartDate())
                .terminationDate(req.getTerminationDate())
                .build());
    }

    private Optional<KeyValue> adRequirementMainCategoryOf(AdRequirementTypeMainCategory adRequirementTypeMainCategory) {
        return Optional.ofNullable(adRequirementTypeMainCategory).map(mainCat ->
                KeyValue.builder()
                        .code(mainCat.getCode())
                        .description(mainCat.getDescription())
                        .build());

    }

    private Optional<KeyValue> requirementTypeSubCategoryOf(StandardReference requirementTypeSubCategory) {
        return Optional.ofNullable(requirementTypeSubCategory).map(subCat ->
                KeyValue.builder()
                        .code(subCat.getCodeValue())
                        .description(subCat.getCodeDescription())
                        .build());
    }

    private Optional<KeyValue> requirementTypeMainCategoryOf(RequirementTypeMainCategory requirementTypeMainCategory) {
        return Optional.ofNullable(requirementTypeMainCategory).map(mainCat ->
                KeyValue.builder()
                        .code(mainCat.getCode())
                        .description(mainCat.getDescription())
                        .build());
    }

    private Optional<KeyValue> adRequirementSubCategoryOf(StandardReference adRequirementTypeSubCategory) {
        return Optional.ofNullable(adRequirementTypeSubCategory).map(adSubCat ->
                KeyValue.builder()
                        .code(adSubCat.getCodeValue())
                        .description(adSubCat.getCodeDescription())
                        .build());
    }

    private Optional<uk.gov.justice.digital.delius.data.api.Nsi> nsiOf(Nsi nsi) {
        return Optional.ofNullable(nsi).map(n ->
                uk.gov.justice.digital.delius.data.api.Nsi.builder()
                        .requirement(requirementOf(n.getRqmnt()))
                        .nsiType(nsiTypeOf(n.getNsiType()))
                        .nsiSubType(nsiSubtypeOf(n.getNsiSubType()))
                        .nsiStatus(nsiStatusOf(n.getNsiStatus()))
                        .build());
    }

    private Optional<KeyValue> nsiStatusOf(NsiStatus nsiStatus) {
        return Optional.ofNullable(nsiStatus).map(nsis ->
                KeyValue.builder().code(nsis.getCode())
                        .description(nsis.getDescription())
                        .build());
    }

    private Optional<KeyValue> nsiSubtypeOf(StandardReference nsiSubType) {
        return Optional.ofNullable(nsiSubType).map(nsist ->
                KeyValue.builder()
                        .code(nsist.getCodeValue())
                        .description(nsist.getCodeDescription())
                        .build());
    }

    private Optional<KeyValue> nsiTypeOf(NsiType nsiType) {
        return Optional.ofNullable(nsiType).map(nsit -> KeyValue.builder()
                .code(nsit.getCode())
                .description(nsit.getDescription())
                .build());
    }

    private Optional<uk.gov.justice.digital.delius.data.api.LicenceCondition> licenceConditionOf(LicenceCondition licenceCondition) {
        return Optional.ofNullable(licenceCondition).map(lc -> uk.gov.justice.digital.delius.data.api.LicenceCondition.builder()
                .active(lc.getActiveFlag() == 1)
                .commencementDate(lc.getCommencementDate())
                .commencementNotes(Optional.ofNullable(lc.getCommencementNotes()))
                .createdDateTime(lc.getCreatedDateTime())
                .licenceConditionNotes(Optional.ofNullable(lc.getLicenceConditionNotes()))
                .licenceConditionTypeMainCat(licenceConditionTypeMainCatOf(lc.getLicenceConditionTypeMainCat()))
                .startDate(lc.getStartDate())
                .terminationDate(lc.getTerminationDate())
                .terminationNotes(Optional.ofNullable(lc.getTerminationNotes()))
                .build());
    }

    private Optional<KeyValue> licenceConditionTypeMainCatOf(LicenceConditionTypeMainCat licenceConditionTypeMainCat) {
        return Optional.ofNullable(licenceConditionTypeMainCat).map(lctmc ->
                KeyValue.builder()
                        .code(lctmc.getCode())
                        .description(lctmc.getDescription())
                        .build());
    }

    private Optional<KeyValue> explanationOf(Explanation explanation) {
        return Optional.ofNullable(explanation).map(e ->
                KeyValue.builder()
                        .code(e.getCode())
                        .description(e.getDescription())
                        .build());
    }

    private uk.gov.justice.digital.delius.data.api.ContactType contactTypeOf(ContactType contactType) {
        return uk.gov.justice.digital.delius.data.api.ContactType.builder()
                .code(contactType.getCode())
                .description(contactType.getDescription())
                .shortDescription(Optional.ofNullable(contactType.getShortDescription()))
                .build();
    }

    private Optional<KeyValue> contactOutcomeTypeOf(ContactOutcomeType contactOutcomeType) {
        return Optional.ofNullable(contactOutcomeType).map(cot ->
                KeyValue.builder()
                        .code(cot.getCode())
                        .description(cot.getDescription())
                        .build());
    }


}

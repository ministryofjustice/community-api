package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.KeyValue;
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
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class ContactTransformer {

    RequirementTransformer requirementTransformer = new RequirementTransformer();

    public List<Contact> contactsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.Contact> contacts) {
        return contacts.stream()
                .sorted(comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Contact::getCreatedDateTime))
                .map(this::contactOf)
                .collect(Collectors.toList());
    }

    private uk.gov.justice.digital.delius.data.api.Contact contactOf(uk.gov.justice.digital.delius.jpa.standard.entity.Contact contact) {
        return uk.gov.justice.digital.delius.data.api.Contact.builder()
                .eventId(eventIdOf(contact.getEvent()))
                .alertActive(ynToBoolean(contact.getAlertActive()))
                .contactEndTime(contact.getContactEndTime())
                .contactId(contact.getContactId())
                .contactOutcomeType(contactOutcomeTypeOf(contact.getContactOutcomeType()))
                .contactStartTime(contact.getContactStartTime())
                .contactType(contactTypeOf(contact.getContactType()))
                .createdDateTime(contact.getCreatedDateTime())
                .explanation(explanationOf(contact.getExplanation()))
                .lastUpdatedDateTime(contact.getLastUpdatedDateTime())
                .licenceCondition(licenceConditionOf(contact.getLicenceCondition()))
                .linkedContactId(contact.getLinkedContactId())
                .notes(contact.getNotes())
                .nsi(nsiOf(contact.getNsi()))
                .requirement(requirementTransformer.requirementOf(contact.getRequirement()))
                .softDeleted(zeroOneToBoolean(contact.getSoftDeleted()))
                .probationArea(probationAreaOf(contact.getProbationArea()))
                .partitionArea(partitionAreaOf(contact.getPartitionArea()))
                .providerEmployee(providerEmployeeOf(contact.getProviderEmployee()))
                .providerLocation(providerLocationOf(contact.getProviderLocation()))
                .providerTeam(providerTeamOf(contact.getProviderTeam()))
                .staff(staffOf(contact.getStaff()))
                .team(teamOf(contact.getTeam()))
                .hoursCredited(contact.getHoursCredited())
                .visorContact(ynToBoolean(contact.getVisorContact()))
                .attended(ynToBoolean(contact.getAttended()))
                .complied(ynToBoolean(contact.getComplied()))
                .uploadLinked(ynToBoolean(contact.getUploadLinked()))
                .documentLinked(ynToBoolean(contact.getDocumentLinked()))
                .build();
    }

    protected Long eventIdOf(Event event) {
        return Optional.ofNullable(event).map(Event::getEventId).orElse(null);
    }

    public KeyValue teamOf(Team team) {
        return Optional.ofNullable(team).map(t -> KeyValue.builder().code(t.getCode()).description(t.getDescription()).build()).orElse(null);
    }

    public Human staffOf(Staff staff) {
        return Optional.ofNullable(staff).map(s -> Human
                .builder()
                .forenames(combinedForenamesOf(s.getForename(), s.getForname2()))
                .surname(s.getSurname())
                .build()).orElse(null);
    }

    protected KeyValue providerTeamOf(ProviderTeam providerTeam) {
        return Optional.ofNullable(providerTeam).map(pt -> KeyValue.builder().code(pt.getCode()).description(pt.getName()).build()).orElse(null);
    }

    protected KeyValue providerLocationOf(ProviderLocation providerLocation) {
        return Optional.ofNullable(providerLocation).map(
                pl -> KeyValue.builder().code(providerLocation.getCode()).description(providerLocation.getDescription()).build()
        ).orElse(null);
    }

    public Human providerEmployeeOf(ProviderEmployee providerEmployee) {
        return Optional.ofNullable(providerEmployee)
                .map(pe -> Human
                        .builder()
                        .forenames(combinedForenamesOf(pe.getForename(), pe.getForname2()))
                        .surname(pe.getSurname())
                        .build()).orElse(null);
    }

    private String combinedForenamesOf(String name1, String name2) {
        Optional<String> maybeSecondName = Optional.ofNullable(name1);
        Optional<String> maybeThirdName = Optional.ofNullable(name2);

        return ImmutableList.of(maybeSecondName, maybeThirdName)
                .stream()
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.joining(" "));
    }

    protected String partitionAreaOf(PartitionArea partitionArea) {
        return Optional.ofNullable(partitionArea).map(PartitionArea::getArea).orElse(null);
    }

    protected KeyValue probationAreaOf(ProbationArea probationArea) {
        return Optional.ofNullable(probationArea).map(
                pa -> KeyValue.builder().code(pa.getCode()).description(pa.getDescription()).build()
        ).orElse(null);
    }

    protected uk.gov.justice.digital.delius.data.api.Nsi nsiOf(Nsi nsi) {
        return Optional.ofNullable(nsi).map(n ->
                uk.gov.justice.digital.delius.data.api.Nsi.builder()
                        .requirement(requirementTransformer.requirementOf(n.getRqmnt()))
                        .nsiType(nsiTypeOf(n.getNsiType()))
                        .nsiSubType(nsiSubtypeOf(n.getNsiSubType()))
                        .nsiStatus(nsiStatusOf(n.getNsiStatus()))
                        .build()).orElse(null);
    }

    private KeyValue nsiStatusOf(NsiStatus nsiStatus) {
        return Optional.ofNullable(nsiStatus).map(nsis ->
                KeyValue.builder().code(nsis.getCode())
                        .description(nsis.getDescription())
                        .build()).orElse(null);
    }

    private KeyValue nsiSubtypeOf(StandardReference nsiSubType) {
        return Optional.ofNullable(nsiSubType).map(nsist ->
                KeyValue.builder()
                        .code(nsist.getCodeValue())
                        .description(nsist.getCodeDescription())
                        .build()).orElse(null);
    }

    private KeyValue nsiTypeOf(NsiType nsiType) {
        return Optional.ofNullable(nsiType).map(nsit -> KeyValue.builder()
                .code(nsit.getCode())
                .description(nsit.getDescription())
                .build()).orElse(null);
    }

    protected uk.gov.justice.digital.delius.data.api.LicenceCondition licenceConditionOf(LicenceCondition licenceCondition) {
        return Optional.ofNullable(licenceCondition).map(lc -> uk.gov.justice.digital.delius.data.api.LicenceCondition.builder()
                .active(zeroOneToBoolean(lc.getActiveFlag()))
                .commencementDate(lc.getCommencementDate())
                .commencementNotes(lc.getCommencementNotes())
                .createdDateTime(lc.getCreatedDateTime())
                .licenceConditionNotes(lc.getLicenceConditionNotes())
                .licenceConditionTypeMainCat(licenceConditionTypeMainCatOf(lc.getLicenceConditionTypeMainCat()))
                .startDate(lc.getStartDate())
                .terminationDate(lc.getTerminationDate())
                .terminationNotes(lc.getTerminationNotes())
                .build()).orElse(null);
    }

    private KeyValue licenceConditionTypeMainCatOf(LicenceConditionTypeMainCat licenceConditionTypeMainCat) {
        return Optional.ofNullable(licenceConditionTypeMainCat).map(lctmc ->
                KeyValue.builder()
                        .code(lctmc.getCode())
                        .description(lctmc.getDescription())
                        .build()).orElse(null);
    }

    protected KeyValue explanationOf(Explanation explanation) {
        return Optional.ofNullable(explanation).map(e ->
                KeyValue.builder()
                        .code(e.getCode())
                        .description(e.getDescription())
                        .build()).orElse(null);
    }

    private uk.gov.justice.digital.delius.data.api.ContactType contactTypeOf(ContactType contactType) {
        return uk.gov.justice.digital.delius.data.api.ContactType.builder()
                .code(contactType.getCode())
                .description(contactType.getDescription())
                .shortDescription(Optional.ofNullable(contactType.getShortDescription()).orElse(null))
                .build();
    }

    private KeyValue contactOutcomeTypeOf(ContactOutcomeType contactOutcomeType) {
        return Optional.ofNullable(contactOutcomeType).map(cot ->
                KeyValue.builder()
                        .code(cot.getCode())
                        .description(cot.getDescription())
                        .build()).orElse(null);
    }


}

package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.ActivityLogGroup;
import uk.gov.justice.digital.delius.data.api.ActivityLogGroup.ActivityLogEntry;
import uk.gov.justice.digital.delius.data.api.AvailableContactOutcomeTypes;
import uk.gov.justice.digital.delius.data.api.ContactOutcomeTypeDetail;
import uk.gov.justice.digital.delius.data.api.ContactRarActivity;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.ContactSummary;
import uk.gov.justice.digital.delius.data.api.Enforcement;
import uk.gov.justice.digital.delius.data.api.EnforcementAction;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.RequiredOptional;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.jpa.standard.YesNoBlank;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Explanation;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceCondition;
import uk.gov.justice.digital.delius.jpa.standard.entity.LicenceConditionTypeMainCat;
import uk.gov.justice.digital.delius.jpa.standard.entity.PartitionArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderEmployee;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderLocation;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProviderTeam;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.entity.User;
import uk.gov.justice.digital.delius.utils.DateConverter;

import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.*;

import static java.util.Comparator.comparing;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

public class ContactTransformer {
    public static ContactSummary contactSummaryOf(uk.gov.justice.digital.delius.jpa.standard.entity.Contact contact) {
        return ContactSummary.builder()
            .contactId(contact.getContactId())
            .contactStart(DateConverter.toOffsetDateTime(contact.getContactDate(), contact.getContactStartTime()))
            .contactEnd(DateConverter.toOffsetDateTime(contact.getContactDate(), contact.getContactEndTime()))
            .type(contactTypeOf(contact.getContactType(), true))
            .officeLocation(Optional.ofNullable(contact.getOfficeLocation())
                .map(OfficeLocationTransformer::officeLocationOf)
                .orElse(null))
            .notes(contact.getNotes())
            .provider(ContactTransformer.probationAreaOf(contact.getProbationArea()))
            .team(ContactTransformer.teamOf(contact.getTeam()))
            .staff(ContactTransformer.staffOf(contact.getStaff()))
            .sensitive(ynToBoolean(contact.getSensitive()))
            .outcome(AppointmentTransformer.appointmentOutcomeOf(contact))
            .rarActivity(contact.isRarActivity())
            .rarActivityDetail(contactRarActivityOf(contact))
            .enforcement(Optional.ofNullable(contact.getEnforcement()).map(ContactTransformer::enforcementOf).orElse(null))
            .lastUpdatedDateTime(Optional.ofNullable(contact.getLastUpdatedDateTime()).map(DateConverter::toOffsetDateTime).orElse(null))
            .lastUpdatedByUser(humanOf(contact.getLastUpdatedByUser()))
            .description(contact.getDescription())
            .build();
    }

    public static List<Contact> contactsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.Contact> contacts) {
        return contacts.stream()
                .sorted(comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Contact::getCreatedDateTime))
                .map(ContactTransformer::contactOf)
                .collect(Collectors.toList());
    }

    public static Nsi nsiOf(uk.gov.justice.digital.delius.jpa.standard.entity.Nsi nsi) {
        return NsiTransformer.nsiOf(nsi);
    }

    public static uk.gov.justice.digital.delius.data.api.ContactType contactTypeOf(ContactType contactType) {
        return contactTypeOf(contactType, false);
    }

    public static uk.gov.justice.digital.delius.data.api.ContactType contactTypeOf(ContactType contactType, boolean includeCategories) {
        var builder = uk.gov.justice.digital.delius.data.api.ContactType.builder()
            .code(contactType.getCode())
            .description(contactType.getDescription())
            .shortDescription(contactType.getShortDescription())
            .appointment(contactType.getAttendanceContact())
            .nationalStandard(contactType.getNationalStandardsContact())
            .systemGenerated(contactType.getSystemGenerated());

            if(includeCategories) {
                builder.categories(Optional.ofNullable(contactType.getContactCategories())
                    .map(categories -> categories.stream()
                        .filter(StandardReference::isActive)
                        .map(KeyValueTransformer::keyValueOf)
                        .collect(Collectors.toList())
                    ).orElse(Collections.emptyList())
                );
            }

            return builder.build();
    }

    public static uk.gov.justice.digital.delius.data.api.Contact contactOf(uk.gov.justice.digital.delius.jpa.standard.entity.Contact contact) {
        return uk.gov.justice.digital.delius.data.api.Contact.builder()
                .eventId(contact.getEventId())
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
                .nsi(NsiTransformer.nsiOf(contact.getNsi()))
                .requirement(RequirementTransformer.requirementOf(contact.getRequirement()))
                .softDeleted(contact.getSoftDeleted())
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

    protected static Long eventIdOf(Event event) {
        return Optional.ofNullable(event).map(Event::getEventId).orElse(null);
    }

    public static KeyValue teamOf(Team team) {
        return Optional.ofNullable(team).map(t -> KeyValue.builder().code(t.getCode()).description(t.getDescription()).build()).orElse(null);
    }

    public static StaffHuman staffOf(Staff staff) {
        return Optional.ofNullable(staff).map(s -> StaffHuman
                .builder()
                .code(s.getOfficerCode())
                .forenames(combinedForenamesOf(s.getForename(), s.getForname2()))
                .surname(s.getSurname())
                .build()).orElse(null);
    }

    protected static KeyValue providerTeamOf(ProviderTeam providerTeam) {
        return Optional.ofNullable(providerTeam).map(pt -> KeyValue.builder().code(pt.getCode()).description(pt.getName()).build()).orElse(null);
    }

    protected static KeyValue providerLocationOf(ProviderLocation providerLocation) {
        return Optional.ofNullable(providerLocation).map(
                pl -> KeyValue.builder().code(providerLocation.getCode()).description(providerLocation.getDescription()).build()
        ).orElse(null);
    }

    public static Human providerEmployeeOf(ProviderEmployee providerEmployee) {
        return Optional.ofNullable(providerEmployee)
                .map(pe -> Human
                        .builder()
                        .forenames(combinedForenamesOf(pe.getForename(), pe.getForname2()))
                        .surname(pe.getSurname())
                        .build()).orElse(null);
    }

    private static String combinedForenamesOf(String name1, String name2) {
        Optional<String> maybeSecondName = Optional.ofNullable(name1);
        Optional<String> maybeThirdName = Optional.ofNullable(name2);

        return Stream.of(maybeSecondName, maybeThirdName)
                .flatMap(Optional::stream)
                .collect(Collectors.joining(" "));
    }

    protected static String partitionAreaOf(PartitionArea partitionArea) {
        return Optional.ofNullable(partitionArea).map(PartitionArea::getArea).orElse(null);
    }

    protected static KeyValue probationAreaOf(ProbationArea probationArea) {
        return Optional.ofNullable(probationArea).map(
                pa -> KeyValue.builder().code(pa.getCode()).description(pa.getDescription()).build()
        ).orElse(null);
    }

    public static uk.gov.justice.digital.delius.data.api.LicenceCondition licenceConditionOf(LicenceCondition licenceCondition) {
        return Optional.ofNullable(licenceCondition).map(lc -> uk.gov.justice.digital.delius.data.api.LicenceCondition.builder()
                .active(zeroOneToBoolean(lc.getActiveFlag()))
                .commencementDate(lc.getCommencementDate())
                .commencementNotes(lc.getCommencementNotes())
                .createdDateTime(lc.getCreatedDateTime())
                .licenceConditionNotes(lc.getLicenceConditionNotes())
                .licenceConditionTypeMainCat(licenceConditionTypeMainCatOf(lc.getLicenceConditionTypeMainCat()))
                .licenceConditionTypeSubCat(KeyValueTransformer.keyValueOf(lc.getLicenceConditionTypeSubCat()))
                .startDate(lc.getStartDate())
                .terminationDate(lc.getTerminationDate())
                .terminationNotes(lc.getTerminationNotes())
                .build()).orElse(null);
    }

    private static KeyValue licenceConditionTypeMainCatOf(LicenceConditionTypeMainCat licenceConditionTypeMainCat) {
        return Optional.ofNullable(licenceConditionTypeMainCat).map(lctmc ->
                KeyValue.builder()
                        .code(lctmc.getCode())
                        .description(lctmc.getDescription())
                        .build()).orElse(null);
    }

    protected static KeyValue explanationOf(Explanation explanation) {
        return Optional.ofNullable(explanation).map(e ->
                KeyValue.builder()
                        .code(e.getCode())
                        .description(e.getDescription())
                        .build()).orElse(null);
    }


    private static KeyValue contactOutcomeTypeOf(ContactOutcomeType contactOutcomeType) {
        return Optional.ofNullable(contactOutcomeType).map(cot ->
                KeyValue.builder()
                        .code(cot.getCode())
                        .description(cot.getDescription())
                        .build()).orElse(null);
    }

    private static Human humanOf(final User user){
        return Optional.ofNullable(user).map(u -> Human.builder()
            .forenames(combinedForenamesOf(u.getForename(), u.getForename2()))
            .surname(u.getSurname())
            .build().capitalise()
        ).orElse(null);
    }

    public static List<ActivityLogGroup> activityLogGroupsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.Contact> contacts) {
        final var groups = contacts.stream().collect(Collectors.groupingBy(
            uk.gov.justice.digital.delius.jpa.standard.entity.Contact::getContactDate,
            Collectors.collectingAndThen(Collectors.toList(), list -> list.stream()
                .map(ContactTransformer::activityLogEntryOf)
                .sorted(Comparator.comparing(e -> Optional.ofNullable(e.getStartTime()).orElse(LocalTime.MIDNIGHT)))
                .collect(Collectors.toList()))
        ));


        return groups.entrySet().stream()
            .map(x -> ActivityLogGroup.builder()
                .date(x.getKey())
                .rarDay(x.getValue().stream().anyMatch(e -> e.getRarActivity() != null))
                .entries(x.getValue())
                .build())
            .sorted(Comparator.comparing(ActivityLogGroup::getDate).reversed())
            .collect(Collectors.toList());
    }

    private static ActivityLogEntry activityLogEntryOf(uk.gov.justice.digital.delius.jpa.standard.entity.Contact contact) {
        return ActivityLogEntry.builder()
            .contactId(contact.getContactId())
            .convictionId(Optional.ofNullable(contact.getEvent()).map(Event::getEventId).orElse(null))
            .startTime(contact.getContactStartTime())
            .endTime(contact.getContactEndTime())
            .type(contactTypeOf(contact.getContactType(), true))
            .notes(contact.getNotes())
            .staff(ContactTransformer.staffOf(contact.getStaff()))
            .sensitive(ynToBoolean(contact.getSensitive()))
            .outcome(AppointmentTransformer.appointmentOutcomeOf(contact))
            .rarActivity(contactRarActivityOf(contact))
            .lastUpdatedDateTime(Optional.ofNullable(contact.getLastUpdatedDateTime()).map(DateConverter::toOffsetDateTime).orElse(null))
            .lastUpdatedByUser(humanOf(contact.getLastUpdatedByUser()))
            .enforcement(Optional.ofNullable(contact.getEnforcement()).map(ContactTransformer::enforcementOf).orElse(null))
            .build();
    }

    private static Enforcement enforcementOf(uk.gov.justice.digital.delius.jpa.standard.entity.Enforcement enforcement) {
        return Enforcement.builder().enforcementAction(KeyValue.builder()
            .code(enforcement.getEnforcementAction().getCode()).description(enforcement.getEnforcementAction().getDescription()).build()).build();
    }

    private static ContactRarActivity contactRarActivityOf(uk.gov.justice.digital.delius.jpa.standard.entity.Contact contact) {
        return Optional.ofNullable(contact.getRarComponent())
            .map(rc -> rc.fold(
                    nsi -> ContactRarActivity.builder().requirementId(nsi.getRqmnt().getRequirementId())
                        .nsiId(nsi.getNsiId())
                        .type(NsiTransformer.nsiTypeOf(nsi.getNsiType()))
                        .subtype(KeyValueTransformer.keyValueOf(nsi.getNsiSubType())),
                    // current policy (Sep-2021) is that RAR recording is done against an NSI, so we can afford to have poor defaults here.
                    r -> ContactRarActivity.builder().requirementId(r.getRequirementId())
                ).build())
            .orElse(null);
    }

    public static AvailableContactOutcomeTypes availableContactOutcomeTypesOf(uk.gov.justice.digital.delius.jpa.standard.entity.ContactType contactType) {
        return Optional.ofNullable(contactType).map(
            ct -> AvailableContactOutcomeTypes.builder()
                .outcomeRequired(toRequiredOptional(ct.getOutcomeFlag()))
                .outcomeTypes(ct.getContactOutcomeTypes()
                    .stream()
                    .map(ContactTransformer::toContactOutcomeTypeDetail).
                    collect(Collectors.toList()))
                .build()
        ).orElse(null);
    }

    private static ContactOutcomeTypeDetail toContactOutcomeTypeDetail(final ContactOutcomeType contactOutcomeType) {
        return Optional.ofNullable(contactOutcomeType).map(ctd -> ContactOutcomeTypeDetail.builder()
                .code(ctd.getCode())
                .description(ctd.getDescription())
                .actionRequired(ctd.getActionRequired())
                .attendance(ctd.getAttendance())
                .compliantAcceptable(ctd.getCompliantAcceptable())
                .enforceable(ctd.getEnforceable())
                .enforcements(Optional.ofNullable(ctd.getEnforcementActions()).map( e -> e.stream()
                    .map(ContactTransformer::toEnforcementAction)
                    .collect(Collectors.toList())).orElse(Collections.emptyList()))
                .build())
            .orElse(null);
    }

    private static EnforcementAction toEnforcementAction(final uk.gov.justice.digital.delius.jpa.standard.entity.EnforcementAction enforcementAction) {
        return Optional.ofNullable(enforcementAction).map(ea -> EnforcementAction.builder()
                .code(ea.getCode())
                .description(ea.getDescription())
                .outstandingContactAction(ea.getOutstandingContactAction())
                .responseByPeriod(ea.getResponseByPeriod())
                .build())
            .orElse(null);
    }

    public static RequiredOptional toRequiredOptional(YesNoBlank value) {
        if (value == null) {
            throw new RuntimeException("Invalid null Yes/No/Blank flag value");
        }
        return switch (value) {
            case Y -> RequiredOptional.REQUIRED;
            case B -> RequiredOptional.OPTIONAL;
            case N -> RequiredOptional.NOT_REQUIRED;
        };
    }
}

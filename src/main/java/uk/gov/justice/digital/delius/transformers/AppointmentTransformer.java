package uk.gov.justice.digital.delius.transformers;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.AppointmentDetail;
import uk.gov.justice.digital.delius.data.api.AppointmentOutcome;
import uk.gov.justice.digital.delius.data.api.AppointmentType;
import uk.gov.justice.digital.delius.data.api.AppointmentType.OrderType;
import uk.gov.justice.digital.delius.data.api.AppointmentType.RequiredOptional;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.OfficeLocation;
import uk.gov.justice.digital.delius.utils.DateConverter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.digital.delius.data.api.Appointment.Attended.ATTENDED;
import static uk.gov.justice.digital.delius.data.api.Appointment.Attended.NOT_RECORDED;
import static uk.gov.justice.digital.delius.data.api.Appointment.Attended.UNATTENDED;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;

public class AppointmentTransformer {

    public static List<Appointment> appointmentsOf(List<Contact> contacts) {
        return contacts.stream()
                .map(AppointmentTransformer::appointmentOf)
                .collect(Collectors.toList());
    }

    public static AppointmentDetail appointmentDetailOf(Contact contact) {
        return AppointmentDetail.builder()
            .appointmentId(contact.getContactId())
            .appointmentStart(DateConverter.toOffsetDateTime(contact.getContactDate(), contact.getContactStartTime()))
            .appointmentEnd(DateConverter.toOffsetDateTime(contact.getContactDate(), contact.getContactEndTime()))
            .type(appointmentTypeOf(contact.getContactType()))
            .officeLocation(Optional.ofNullable(contact.getOfficeLocation())
                .map(OfficeLocationTransformer::officeLocationOf)
                .orElse(null))
            .notes(contact.getNotes())
            .provider(ContactTransformer.probationAreaOf(contact.getProbationArea()))
            .team(ContactTransformer.teamOf(contact.getTeam()))
            .staff(ContactTransformer.staffOf(contact.getStaff()))
            .sensitive(ynToBoolean(contact.getSensitive()))
            .outcome(appointmentOutcomeOf(contact))
            .build();
    }

    public static AppointmentOutcome appointmentOutcomeOf(Contact contact) {
        return Optional.ofNullable(contact.getContactOutcomeType())
            .map(type -> AppointmentOutcome.builder()
                .code(type.getCode())
                .description(type.getDescription())

                // the following fields are taken from the contact as they are de-normalised there during contact creation.
                // these copies seem to be the canonical source.
                .attended(ynToBoolean(contact.getAttended()))
                .complied(ynToBoolean(contact.getComplied()))
                .hoursCredited(contact.getHoursCredited())
                .build())
            .orElse(null);
    }

    public static AppointmentType appointmentTypeOf(ContactType type) {
        return AppointmentType.builder()
            .contactType(type.getCode())
            .description(type.getDescription())
            .requiresLocation(locationFlagToRequiredOptional(type.getLocationFlag()))
            .orderTypes(Stream.of(
                Pair.of(OrderType.CJA, type.getCjaOrderLevel()),
                Pair.of(OrderType.LEGACY, type.getLegacyOrderLevel())
            ).filter(x -> x.getValue().equals("Y")).map(Pair::getKey).collect(Collectors.toList()))
            .build();
    }

    private static Appointment appointmentOf(Contact contact) {
        return Appointment.builder()
            .eventId(ContactTransformer.eventIdOf(contact.getEvent()))
            .alertActive(ynToBoolean(contact.getAlertActive()))
            .appointmentDate(contact.getContactDate())
            .appointmentStartTime(contact.getContactStartTime())
            .appointmentEndTime(contact.getContactEndTime())
            .appointmentId(contact.getContactId())
            .appointmentOutcomeType(keyValueOf(contact.getContactOutcomeType()))
            .appointmentType(keyValueOf(contact.getContactType()))
            .createdDateTime(contact.getCreatedDateTime())
            .explanation(ContactTransformer.explanationOf(contact.getExplanation()))
            .lastUpdatedDateTime(contact.getLastUpdatedDateTime())
            .licenceCondition(ContactTransformer.licenceConditionOf(contact.getLicenceCondition()))
            .linkedContactId(contact.getLinkedContactId())
            .notes(contact.getNotes())
            .nsi(ContactTransformer.nsiOf(contact.getNsi()))
            .requirement(RequirementTransformer.requirementOf(contact.getRequirement()))
            .probationArea(ContactTransformer.probationAreaOf(contact.getProbationArea()))
            .providerEmployee(ContactTransformer.providerEmployeeOf(contact.getProviderEmployee()))
            .officeLocation(keyValueOf(contact.getOfficeLocation()))
            .providerLocation(ContactTransformer.providerLocationOf(contact.getProviderLocation()))
            .team(ContactTransformer.teamOf(contact.getTeam()))
            .providerTeam(ContactTransformer.providerTeamOf(contact.getProviderTeam()))
            .staff(ContactTransformer.staffOf(contact.getStaff()))
            .hoursCredited(contact.getHoursCredited())
            .visorContact(ynToBoolean(contact.getVisorContact()))
            .attended(attendedOf(contact.getAttended()))
            .complied(ynToBoolean(contact.getComplied()))
            .uploadLinked(ynToBoolean(contact.getUploadLinked()))
            .documentLinked(ynToBoolean(contact.getDocumentLinked()))
            .build();
    }

    private static KeyValue keyValueOf(ContactOutcomeType contactOutcomeType) {
        return Optional.ofNullable(contactOutcomeType).map(cot ->
            KeyValue.builder()
                .code(cot.getCode())
                .description(cot.getDescription())
                .build()).orElse(null);
    }

    private static Appointment.Attended attendedOf(String yn) {
        return Optional.ofNullable(ynToBoolean(yn)).map(flag -> flag ? ATTENDED : UNATTENDED).orElse(NOT_RECORDED);
    }

    private static KeyValue keyValueOf(ContactType contactType) {
        return KeyValue.builder()
            .code(contactType.getCode())
            .description(contactType.getDescription())
            .build();
    }

    private static KeyValue keyValueOf(OfficeLocation officeLocation) {
        return Optional.ofNullable(officeLocation).map(
            pl -> KeyValue.builder().code(officeLocation.getCode()).description(officeLocation.getDescription()).build()
        ).orElse(null);
    }

    private static RequiredOptional locationFlagToRequiredOptional(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "Y" -> RequiredOptional.REQUIRED;
            case "B" -> RequiredOptional.OPTIONAL;
            case "N" -> RequiredOptional.NOT_REQUIRED;
            default -> throw new RuntimeException(String.format("Invalid location flag '%s'", value));
        };
    }
}

package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.OfficeLocation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.data.api.Appointment.Attended.ATTENDED;
import static uk.gov.justice.digital.delius.data.api.Appointment.Attended.NOT_RECORDED;
import static uk.gov.justice.digital.delius.data.api.Appointment.Attended.UNATTENDED;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;

public class AppointmentTransformer {

    public static List<Appointment> appointmentsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.Contact> contacts) {
        return contacts.stream()
                .map(AppointmentTransformer::appointmentOf)
                .collect(Collectors.toList());
    }

    private static Appointment appointmentOf(uk.gov.justice.digital.delius.jpa.standard.entity.Contact contact) {
        return Appointment.builder()
                .eventId(ContactTransformer.eventIdOf(contact.getEvent()))
                .alertActive(ynToBoolean(contact.getAlertActive()))
                .appointmentDate(contact.getContactDate())
                .appointmentStartTime(contact.getContactStartTime())
                .appointmentEndTime(contact.getContactEndTime())
                .appointmentId(contact.getContactId())
                .appointmentOutcomeType(appointmentOutcomeTypeOf(contact.getContactOutcomeType()))
                .appointmentType(appointmentTypeOf(contact.getContactType()))
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
                .officeLocation(officeLocationOf(contact.getOfficeLocation()))
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

    private static KeyValue appointmentOutcomeTypeOf(ContactOutcomeType contactOutcomeType) {
        return Optional.ofNullable(contactOutcomeType).map(cot ->
                KeyValue.builder()
                        .code(cot.getCode())
                        .description(cot.getDescription())
                        .build()).orElse(null);
    }

    private static Appointment.Attended attendedOf(String yn) {
       return Optional.ofNullable(ynToBoolean(yn)).map(flag -> flag ? ATTENDED : UNATTENDED).orElse(NOT_RECORDED);
    }

    private static KeyValue appointmentTypeOf(ContactType contactType) {
        return KeyValue.builder()
                .code(contactType.getCode())
                .description(contactType.getDescription())
                .build();
    }

    private static KeyValue officeLocationOf(OfficeLocation officeLocation) {
        return Optional.ofNullable(officeLocation).map(
                pl -> KeyValue.builder().code(officeLocation.getCode()).description(officeLocation.getDescription()).build()
        ).orElse(null);
    }

}

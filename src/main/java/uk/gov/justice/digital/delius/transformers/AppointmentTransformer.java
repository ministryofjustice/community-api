package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.OfficeLocation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.data.api.Appointment.Attended.*;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;

@Component
public class AppointmentTransformer {
    private final ContactTransformer contactTransformer;

    public AppointmentTransformer(ContactTransformer contactTransformer) {
        this.contactTransformer = contactTransformer;
    }


    public List<Appointment> appointmentsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.Contact> contacts) {
        return contacts.stream()
                .map(this::appointmentOf)
                .collect(Collectors.toList());
    }

    private Appointment appointmentOf(uk.gov.justice.digital.delius.jpa.standard.entity.Contact contact) {
        return Appointment.builder()
                .eventId(contactTransformer.eventIdOf(contact.getEvent()))
                .alertActive(ynToBoolean(contact.getAlertActive()))
                .appointmentDate(contact.getContactDate())
                .appointmentStartTime(contact.getContactStartTime())
                .appointmentEndTime(contact.getContactEndTime())
                .appointmentId(contact.getContactId())
                .appointmentOutcomeType(appointmentOutcomeTypeOf(contact.getContactOutcomeType()))
                .appointmentType(appointmentTypeOf(contact.getContactType()))
                .createdDateTime(contact.getCreatedDateTime())
                .explanation(contactTransformer.explanationOf(contact.getExplanation()))
                .lastUpdatedDateTime(contact.getLastUpdatedDateTime())
                .licenceCondition(contactTransformer.licenceConditionOf(contact.getLicenceCondition()))
                .linkedContactId(contact.getLinkedContactId())
                .notes(contact.getNotes())
                .nsi(contactTransformer.nsiOf(contact.getNsi()))
                .requirement(contactTransformer.requirementTransformer.requirementOf(contact.getRequirement()))
                .probationArea(contactTransformer.probationAreaOf(contact.getProbationArea()))
                .providerEmployee(contactTransformer.providerEmployeeOf(contact.getProviderEmployee()))
                .officeLocation(officeLocationOf(contact.getOfficeLocation()))
                .providerLocation(contactTransformer.providerLocationOf(contact.getProviderLocation()))
                .team(contactTransformer.teamOf(contact.getTeam()))
                .providerTeam(contactTransformer.providerTeamOf(contact.getProviderTeam()))
                .staff(contactTransformer.staffOf(contact.getStaff()))
                .hoursCredited(contact.getHoursCredited())
                .visorContact(ynToBoolean(contact.getVisorContact()))
                .attended(attendedOf(contact.getAttended()))
                .complied(ynToBoolean(contact.getComplied()))
                .uploadLinked(ynToBoolean(contact.getUploadLinked()))
                .documentLinked(ynToBoolean(contact.getDocumentLinked()))
                .build();
    }

    private KeyValue appointmentOutcomeTypeOf(ContactOutcomeType contactOutcomeType) {
        return Optional.ofNullable(contactOutcomeType).map(cot ->
                KeyValue.builder()
                        .code(cot.getCode())
                        .description(cot.getDescription())
                        .build()).orElse(null);
    }

    private Appointment.Attended attendedOf(String yn) {
       return Optional.ofNullable(ynToBoolean(yn)).map(flag -> flag ? ATTENDED : UNATTENDED).orElse(NOT_RECORDED);
    }

    private KeyValue appointmentTypeOf(ContactType contactType) {
        return KeyValue.builder()
                .code(contactType.getCode())
                .description(contactType.getDescription())
                .build();
    }

    private KeyValue officeLocationOf(OfficeLocation officeLocation) {
        return Optional.ofNullable(officeLocation).map(
                pl -> KeyValue.builder().code(officeLocation.getCode()).description(officeLocation.getDescription()).build()
        ).orElse(null);
    }

}

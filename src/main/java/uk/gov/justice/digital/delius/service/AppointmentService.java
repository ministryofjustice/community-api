package uk.gov.justice.digital.delius.service;

import com.github.fge.jsonpatch.JsonPatch;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentUpdateResponse;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.transformers.AppointmentTransformer;
import uk.gov.justice.digital.delius.utils.DateConverter;
import uk.gov.justice.digital.delius.utils.JsonPatchSupport;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static uk.gov.justice.digital.delius.transformers.AppointmentCreateRequestTransformer.appointmentOf;
import static uk.gov.justice.digital.delius.transformers.AppointmentPatchRequestTransformer.mapAttendanceFieldsToOutcomeOf;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalTime;

@Service
@AllArgsConstructor
public class AppointmentService {

    private final ContactTypeRepository contactTypeRepository;
    private final ContactRepository contactRepository;
    private final RequirementService requirementService;
    private final DeliusApiClient deliusApiClient;
    private final DeliusIntegrationContextConfig deliusIntegrationContextConfig;
    private final JsonPatchSupport jsonPatchSupport;

    public List<Appointment> appointmentsFor(Long offenderId, AppointmentFilter filter) {
        return AppointmentTransformer.appointmentsOf(
                contactRepository.findAll(
                        filter.toBuilder().offenderId(offenderId).build(),
                        Sort.by(DESC, "contactDate")));
    }

    public AppointmentCreateResponse createAppointment(String crn, Long sentenceId, AppointmentCreateRequest request) {
        this.assertAppointmentType(request.getContactType());

        final var newContact = makeNewContact(crn, sentenceId, request);
        final var contactDto = deliusApiClient.createNewContact(newContact);

        return makeResponse(contactDto);
    }

    public AppointmentCreateResponse createAppointment(String crn, Long sentenceId, String contextName, ContextlessAppointmentCreateRequest contextualRequest) {

        final var context = getContext(contextName);
        final var requirement = requirementService.getRequirement(crn, sentenceId, context.getRequirementRehabilitationActivityType());
        final var request = appointmentOf(contextualRequest, requirement, context);

        return createAppointment(crn, sentenceId, request);
    }

    public AppointmentUpdateResponse patchAppointment(String crn, Long appointmentId, JsonPatch jsonPatch) {

        this.assertAppointmentTypeIfExists(jsonPatch);
        final var contactDto = deliusApiClient.patchContact(appointmentId, jsonPatch);
        return new AppointmentUpdateResponse(contactDto.getId());
    }

    public AppointmentUpdateResponse updateAppointmentOutcome(String crn, Long appointmentId, String contextName, ContextlessAppointmentOutcomeRequest request) {

        final var context = getContext(contextName);
        final var mappedJsonPatch = mapAttendanceFieldsToOutcomeOf(request, context);
        return patchAppointment(crn, appointmentId, mappedJsonPatch);
    }

    private void assertAppointmentType(String contactTypeCode) {
        final var type = this.contactTypeRepository.findByCode(contactTypeCode)
            .orElseThrow(() -> new BadRequestException(String.format("contact type '%s' does not exist", contactTypeCode)));

        if (!type.getAttendanceContact().equals("Y")) {
            throw new BadRequestException(String.format("contact type '%s' is not an appointment type", contactTypeCode));
        }
    }

    private void assertAppointmentTypeIfExists(JsonPatch jsonPatch) {
        jsonPatchSupport.getAsText("/contactType", jsonPatch).ifPresent(
            contactTypeCode -> assertAppointmentType(contactTypeCode));
    }

    private NewContact makeNewContact(String crn, Long sentenceId, AppointmentCreateRequest request) {
        return NewContact.builder()
            .offenderCrn(crn)
            .type(request.getContactType())
            .provider(request.getProviderCode())
            .team(request.getTeamCode())
            .staff(request.getStaffCode())
            .officeLocation(request.getOfficeLocationCode())
            .date(toLondonLocalDate(request.getAppointmentStart()))
            .startTime(toLondonLocalTime(request.getAppointmentStart()))
            .endTime(toLondonLocalTime(request.getAppointmentEnd()))
            .notes(request.getNotes())
            .eventId(sentenceId)
            .requirementId(request.getRequirementId())
            .build();
    }

    private AppointmentCreateResponse makeResponse(ContactDto contactDto) {
        var appointmentStart = DateConverter.toOffsetDateTime(contactDto.getDate().atTime(contactDto.getStartTime()));
        var appointmentEnd = DateConverter.toOffsetDateTime(contactDto.getDate().atTime(contactDto.getEndTime()));
        return new AppointmentCreateResponse(contactDto.getId(), appointmentStart, appointmentEnd, contactDto.getType(), contactDto.getTypeDescription());
    }

    IntegrationContext getContext(String name) {
        var context = deliusIntegrationContextConfig.getIntegrationContexts().get(name);
        return Optional.ofNullable(context).orElseThrow(
            () -> new IllegalArgumentException("IntegrationContext does not exist for: " + name)
        );
    }
}

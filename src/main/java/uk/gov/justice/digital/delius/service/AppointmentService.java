package uk.gov.justice.digital.delius.service;

import com.github.fge.jsonpatch.JsonPatch;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentDetail;
import uk.gov.justice.digital.delius.data.api.AppointmentRelocateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentRelocateResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentRescheduleRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentRescheduleResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentType;
import uk.gov.justice.digital.delius.data.api.AppointmentUpdateResponse;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentRescheduleRequest;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.ReplaceContact;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.transformers.AppointmentTransformer;
import uk.gov.justice.digital.delius.utils.DateConverter;
import uk.gov.justice.digital.delius.utils.JsonPatchSupport;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static uk.gov.justice.digital.delius.transformers.AppointmentCreateRequestTransformer.appointmentOf;
import static uk.gov.justice.digital.delius.transformers.AppointmentPatchRequestTransformer.mapAttendanceFieldsToOutcomeOf;
import static uk.gov.justice.digital.delius.transformers.AppointmentPatchRequestTransformer.mapOfficeLocation;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalTime;

@Service
@AllArgsConstructor
public class AppointmentService {

    private final ContactTypeRepository contactTypeRepository;
    private final ContactRepository contactRepository;
    private final ReferralService referralService;
    private final DeliusApiClient deliusApiClient;
    private final DeliusIntegrationContextConfig deliusIntegrationContextConfig;
    private final JsonPatchSupport jsonPatchSupport;

    public List<AppointmentDetail> appointmentDetailsFor(Long offenderId, AppointmentFilter filter) {
        final var contacts = contactRepository.findAll(
            filter.toBuilder().offenderId(offenderId).build(),
            Sort.by(DESC, "contactDate", "contactStartTime", "contactEndTime"));
        return contacts.stream().map(AppointmentTransformer::appointmentDetailOf).collect(Collectors.toList());
    }

    /**
     * Get appointment by offender id & appointment (contact) id.
     * This effectively validates that the appointment is associated to the specified offender.
     */
    public Optional<AppointmentDetail> getAppointment(Long appointmentId, Long offenderId) {
        return contactRepository.findByContactIdAndOffenderIdAndContactTypeAttendanceContactIsTrueAndSoftDeletedIsFalse(appointmentId, offenderId)
            .map(AppointmentTransformer::appointmentDetailOf);
    }

    @Transactional
    public AppointmentCreateResponse createAppointment(String crn, Long sentenceId, AppointmentCreateRequest request) {
        this.assertAppointmentType(request.getContactType());

        final var newContact = makeNewContact(crn, sentenceId, request);
        final var contactDto = deliusApiClient.createNewContact(newContact);

        return makeResponse(contactDto);
    }

    @Transactional
    public AppointmentCreateResponse createAppointment(String crn, Long sentenceId, String contextName, ContextlessAppointmentCreateRequest contextlessRequest) {

        final var context = getContext(contextName);
        final var request = referralService.getExistingMatchingNsi(crn, contextName, sentenceId, contextlessRequest.getContractType(), contextlessRequest.getReferralStart(), contextlessRequest.getReferralId())
            .map(existingNsi -> appointmentOf(contextlessRequest, existingNsi, context))
            .orElseThrow(() -> new BadRequestException(format("Cannot find NSI for CRN: %s Sentence: %d and ContractType %s", crn, sentenceId, contextlessRequest.getContractType())));

        return createAppointment(crn, sentenceId, request);
    }

    @Transactional
    public AppointmentRescheduleResponse rescheduleAppointment(String crn, Long appointmentId, AppointmentRescheduleRequest request) {

        final var existingContact = contactRepository.findById(appointmentId)
            .orElseThrow(() -> new BadRequestException(format("Cannot find Appointment for CRN: %s and Appointment Id %d", crn, appointmentId)));
        final var replacementContact = makeReplacementContact(crn, existingContact, request);
        final var contactDto = deliusApiClient.replaceContact(appointmentId, replacementContact);

        return AppointmentRescheduleResponse.builder().appointmentId(contactDto.getId()).build();
    }

    @Transactional
    public AppointmentRescheduleResponse rescheduleAppointment(String crn, Long appointmentId, String contextName, ContextlessAppointmentRescheduleRequest contextlessRequest) {

        final var outcomeTypeMapping = getContext(contextName).getContactMapping().getInitiatedByServiceProviderToOutcomeType();
        final var rescheduleRequest = ofNullable(outcomeTypeMapping.get(contextlessRequest.getInitiatedByServiceProvider()))
            .map(outcomeType -> AppointmentRescheduleRequest.builder()
                .updatedAppointmentStart(contextlessRequest.getUpdatedAppointmentStart())
                .updatedAppointmentEnd(contextlessRequest.getUpdatedAppointmentEnd())
                .outcome(outcomeType)
                .officeLocationCode(contextlessRequest.getOfficeLocationCode())
                .build())
            .orElseThrow(() -> new BadRequestException(format("Cannot find rescheduled outcome type for initiated-by-Service-Provider: %s", contextlessRequest.getInitiatedByServiceProvider())));

        return rescheduleAppointment(crn, appointmentId, rescheduleRequest);
    }

    @Transactional
    public AppointmentRelocateResponse relocateAppointment(String crn, Long appointmentId, AppointmentRelocateRequest request) {

        contactRepository.findById(appointmentId).orElseThrow(() -> new BadRequestException(format("Cannot find Appointment for CRN: %s and Appointment Id %d", crn, appointmentId)));

        final var jsonPatch = mapOfficeLocation(request.getOfficeLocationCode());
        final var contactDto = deliusApiClient.patchContact(appointmentId, jsonPatch);
        return new AppointmentRelocateResponse(contactDto.getId());
    }

    @Transactional
    public AppointmentUpdateResponse patchAppointment(String crn, Long appointmentId, JsonPatch jsonPatch) {

        this.assertAppointmentTypeIfExists(jsonPatch);
        final var contactDto = deliusApiClient.patchContact(appointmentId, jsonPatch);
        return new AppointmentUpdateResponse(contactDto.getId());
    }

    @Transactional
    public AppointmentUpdateResponse updateAppointmentOutcome(String crn, Long appointmentId, String contextName, ContextlessAppointmentOutcomeRequest request) {

        final var context = getContext(contextName);
        final var mappedJsonPatch = mapAttendanceFieldsToOutcomeOf(request, context);
        return patchAppointment(crn, appointmentId, mappedJsonPatch);
    }

    private void assertAppointmentType(String contactTypeCode) {
        final var type = this.contactTypeRepository.findByCode(contactTypeCode)
            .orElseThrow(() -> new BadRequestException(format("contact type '%s' does not exist", contactTypeCode)));

        if (!type.getAttendanceContact()) {
            throw new BadRequestException(format("contact type '%s' is not an appointment type", contactTypeCode));
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
            .nsiId(request.getNsiId())
            .eventId(sentenceId)
            .requirementId(request.getRequirementId())
            .sensitive(request.getSensitive())
            .rarActivity(request.getRarActivity())
            .outcome(request.getOutcome())
            .enforcement(request.getEnforcement())
            .build();
    }

    private ReplaceContact makeReplacementContact(String crn, Contact existingContact, AppointmentRescheduleRequest request) {
        return ReplaceContact.builder()
            .offenderCrn(crn)
            .outcome(request.getOutcome())
            .date(toLondonLocalDate(request.getUpdatedAppointmentStart()))
            .startTime(toLondonLocalTime(request.getUpdatedAppointmentStart()))
            .endTime(toLondonLocalTime(request.getUpdatedAppointmentEnd()))
            .officeLocation(request.getOfficeLocationCode())
            .nsiId(existingContact.getNsi().getNsiId())
            .eventId(existingContact.getEvent().getEventId())
            .build();
    }

    private AppointmentCreateResponse makeResponse(ContactDto contactDto) {
        var appointmentStart = DateConverter.toOffsetDateTime(contactDto.getDate().atTime(contactDto.getStartTime()));
        var appointmentEnd = DateConverter.toOffsetDateTime(contactDto.getDate().atTime(contactDto.getEndTime()));
        return new AppointmentCreateResponse(contactDto.getId(), appointmentStart, appointmentEnd, contactDto.getType(),
            contactDto.getTypeDescription(), contactDto.getSensitive());
    }

    IntegrationContext getContext(String name) {
        var context = deliusIntegrationContextConfig.getIntegrationContexts().get(name);
        return ofNullable(context).orElseThrow(
            () -> new IllegalArgumentException("IntegrationContext does not exist for: " + name)
        );
    }
}

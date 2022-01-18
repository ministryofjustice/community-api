package uk.gov.justice.digital.delius.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.Appointment.Attended;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentDetail;
import uk.gov.justice.digital.delius.data.api.AppointmentRelocateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentType;
import uk.gov.justice.digital.delius.data.api.AppointmentType.OrderType;
import uk.gov.justice.digital.delius.data.api.RequiredOptional;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentRescheduleRequest;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.Requirement;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.ReplaceContact;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.YesNoBlank;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType.ContactTypeBuilder;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.util.EntityHelper;
import uk.gov.justice.digital.delius.utils.JsonPatchSupport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static com.fasterxml.jackson.databind.node.TextNode.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static uk.gov.justice.digital.delius.jpa.standard.YesNoBlank.B;
import static uk.gov.justice.digital.delius.jpa.standard.YesNoBlank.N;
import static uk.gov.justice.digital.delius.jpa.standard.YesNoBlank.Y;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalTime;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {
    private static final String CONTRACT_TYPE = "ACC";
    private static final String PROVIDER_CODE = "CRS";
    private static final String STAFF_CODE = "CRSUATU";
    private static final String TEAM_CODE = "CRSUAT";
    private static final String RAR_TYPE_CODE = "F";
    private static final String RAR_CONTACT_TYPE = "CRSAPT";
    private static final String SP_INITIATED_RESCHEDULE = "RSSR";
    private static final String OF_INITIATED_RESCHEDULE = "RSOF";
    private static final String ROM_ENFORCEMENT = "ROM";
    private static final String CONTEXT = "commissioned-rehabilitation-services";
    private static final Long EVENT_ID = 99L;
    private static final Long NSI_ID = 98L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ReferralService referralService;
    @Mock
    private DeliusApiClient deliusApiClient;
    @Mock
    private ContactTypeRepository contactTypeRepository;
    @Mock
    private JsonPatchSupport jsonPatchSupport;

    @Captor
    private ArgumentCaptor<Specification<Contact>> specificationArgumentCaptor;
    @Captor
    private ArgumentCaptor<Sort> sortArgumentCaptor;
    private AppointmentService service;

    @BeforeEach
    public void before(){
        final var integrationContextConfig = new DeliusIntegrationContextConfig();
        IntegrationContext integrationContext = new IntegrationContext();
        integrationContextConfig.getIntegrationContexts().put(CONTEXT, integrationContext);
        integrationContext.setProviderCode(PROVIDER_CODE);
        integrationContext.setStaffCode(STAFF_CODE);
        integrationContext.setTeamCode(TEAM_CODE);
        integrationContext.setRequirementRehabilitationActivityType(RAR_TYPE_CODE);
        integrationContext.getContactMapping().setAppointmentRarContactType(RAR_CONTACT_TYPE);
        integrationContext.getContactMapping().setEnforcementReferToOffenderManager(ROM_ENFORCEMENT);
        integrationContext.getContactMapping().setAttendanceAndBehaviourNotifiedMappingToOutcomeType(
            new HashMap<>() {{
                this.put("late", new HashMap<>() {{
                    this.put(false, "ATTC");
                    this.put(true, "ATTC");
                }});
            }}
        );
        integrationContext.getContactMapping().setInitiatedByServiceProviderToOutcomeType(
            new HashMap<>() {{
                this.put(true, SP_INITIATED_RESCHEDULE);
                this.put(false, OF_INITIATED_RESCHEDULE);
            }}
        );

        service = new AppointmentService(contactTypeRepository, contactRepository, referralService, deliusApiClient,
            integrationContextConfig, jsonPatchSupport);
    }

    @Nested
    class FindAppointments {

        @Test
        public void gettingAppointments() {
            final var contacts = List.of(
                EntityHelper.aContact().toBuilder().contactId(1L).build(),
                EntityHelper.aContact().toBuilder().contactId(2L).build());
            final var filter = anAppointmentFilter();
            when(contactRepository.findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture()))
                .thenReturn(contacts);

            final var observed = service.appointmentsFor(1L, filter);

            assertThat(sortArgumentCaptor.getValue()).isEqualTo(Sort.by(DESC, "contactDate"));
            assertThat(specificationArgumentCaptor.getValue()).isEqualTo(filter.toBuilder().offenderId(1L).build());
            assertThat(observed).hasSize(2).extracting("appointmentId", Long.class).containsExactly(1L, 2L);
        }

        @Test
        public void gettingAppointmentDetails() {
            final var contacts = List.of(
                EntityHelper.aContact().toBuilder().contactId(1L).build(),
                EntityHelper.aContact().toBuilder().contactId(2L).build());
            final var filter = anAppointmentFilter();
            when(contactRepository.findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture()))
                .thenReturn(contacts);

            final var observed = service.appointmentDetailsFor(1L, filter);

            assertThat(sortArgumentCaptor.getValue()).isEqualTo(Sort.by(DESC, "contactDate", "contactStartTime", "contactEndTime"));
            assertThat(specificationArgumentCaptor.getValue()).isEqualTo(filter.toBuilder().offenderId(1L).build());
            assertThat(observed).hasSize(2).extracting("appointmentId", Long.class).containsExactly(1L, 2L);
        }

        @Test
        public void gettingAppointment() {
            final var contact = EntityHelper.aContact().toBuilder().contactId(200L).build();
            when(contactRepository.findByContactIdAndOffenderIdAndContactTypeAttendanceContactIsTrueAndSoftDeletedIsFalse( 100L, 200L)).thenReturn(Optional.of(contact));
            final var observed = service.getAppointment(100L, 200L);
            assertThat(observed).isPresent().map(AppointmentDetail::getAppointmentId).hasValue(200L);
        }

        @Test
        public void gettingMissingAppointment() {
            when(contactRepository.findByContactIdAndOffenderIdAndContactTypeAttendanceContactIsTrueAndSoftDeletedIsFalse(100L, 200L)).thenReturn(Optional.empty());
            final var observed = service.getAppointment(100L, 200L);
            assertThat(observed).isNotPresent();
        }

        private AppointmentFilter anAppointmentFilter() {
            return AppointmentFilter.builder()
                .from(Optional.of(LocalDate.of(2021, 5, 26)))
                .to(Optional.of(LocalDate.of(2021, 6, 2)))
                .attended(Optional.of(Attended.ATTENDED))
                .build();
        }
    }

    @Nested
    class CreateAppointments {

        @Test
        public void createsAppointment() {
            // Given
            final var startTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var endTime = startTime.plusHours(1);

            havingContactType(true, builder -> builder.attendanceContact(true), RAR_CONTACT_TYPE);

            final var deliusNewContactRequest = aDeliusNewContactRequest(startTime, endTime, null, true);
            final var createdContact = ContactDto.builder().id(3L).typeDescription("Office Visit")
                .date(LocalDate.of(2021, 1, 31))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();
            when(deliusApiClient.createNewContact(deliusNewContactRequest,"false")).thenReturn(createdContact);

            // When
            final var appointmentCreateRequest = aAppointmentCreateRequest(startTime, endTime, null, true, true);
            final var response = service.createAppointment("X007", 1L, appointmentCreateRequest,"false");

            // Then
            assertThat(response.getAppointmentId()).isEqualTo(3L);
        }

        @Test
        public void failsToCreateAppointmentWithMissingContactType() {
            // Given
            final var startTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var endTime = startTime.plusHours(1);

            havingContactType(false, builder -> builder, RAR_CONTACT_TYPE);

            // When
            final var appointmentCreateRequest = aAppointmentCreateRequest(startTime, endTime, NSI_ID, false, null);
            assertThrows(BadRequestException.class,
                () -> service.createAppointment("X007", 1L, appointmentCreateRequest,"false"),
                "contact type 'X007' does not exist");
        }

        @Test
        public void failsToCreateAppointmentWithNonAppointmentContactType() {
            // Given
            final var startTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var endTime = startTime.plusHours(1);

            havingContactType(true, builder -> builder.attendanceContact(false), RAR_CONTACT_TYPE);

            // When
            final var appointmentCreateRequest = aAppointmentCreateRequest(startTime, endTime, NSI_ID, false, null);
            assertThrows(BadRequestException.class,
                () -> service.createAppointment("X007", 1L, appointmentCreateRequest,"false"),
                "contact type 'X007' is not an appointment type");
        }

        @Test
        public void createsAppointmentUsingContextlessClientRequest() {
            // Given
            final var referralId = UUID.randomUUID();
            final var referralStart = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var startTime = referralStart.plusMinutes(2);
            final var endTime = startTime.plusHours(1);

            final Nsi nsi = aNsiWithRARRequirement();
            when(referralService.getExistingMatchingNsi("X007", CONTEXT, 1L, CONTRACT_TYPE, referralStart, referralId))
                .thenReturn(of(nsi));

            havingContactType(true, builder -> builder.attendanceContact(true), RAR_CONTACT_TYPE);

            final var deliusNewContactRequest = aDeliusNewContactRequest(startTime, endTime, NSI_ID, null);
            final var createdContact = ContactDto.builder().id(3L)
                .date(LocalDate.of(2021, 1, 31))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();
            when(deliusApiClient.createNewContact(deliusNewContactRequest,"false")).thenReturn(createdContact);

            // When
            final var appointmentCreateRequest = aContextlessAppointmentCreateRequest(referralStart, startTime, endTime, CONTRACT_TYPE, referralId);
            final var response = service.createAppointment("X007", 1L, CONTEXT, appointmentCreateRequest,"false");

            // Then
            assertThat(response.getAppointmentId()).isEqualTo(3L);
        }


        @Test
        public void createsHistoricAppointmentUsingContextlessClientRequest() {
            // Given
            final var referralId = UUID.randomUUID();
            final var referralStart = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var startTime = referralStart.plusMinutes(2);
            final var endTime = startTime.plusHours(1);

            final Nsi nsi = aNsiWithRARRequirement();
            when(referralService.getExistingMatchingNsi("X007", CONTEXT, 1L, CONTRACT_TYPE, referralStart, referralId))
                .thenReturn(of(nsi));

            havingContactType(true, builder -> builder.attendanceContact(true), RAR_CONTACT_TYPE);

            final var deliusNewContactRequest = aDeliusNewContactRequest(startTime, endTime, NSI_ID, null, "ATTC", "ROM");
            final var createdContact = ContactDto.builder().id(3L)
                .date(LocalDate.of(2021, 1, 31))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();
            when(deliusApiClient.createNewContact(deliusNewContactRequest,"false")).thenReturn(createdContact);

            // When
            final var appointmentCreateRequest = aContextlessAppointmentCreateRequest(referralStart, startTime, endTime, CONTRACT_TYPE, referralId, "LATE", true);
            final var response = service.createAppointment("X007", 1L, CONTEXT, appointmentCreateRequest,"false");

            // Then
            assertThat(response.getAppointmentId()).isEqualTo(3L);
        }

        @Test
        public void failsToCreateAppointmentFromContextlessRequestWhenNsiDoesNotExist() {
            // Given
            final var referralStart = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var startTime = referralStart.plusMinutes(2);
            final var endTime = startTime.plusHours(1);

            when(referralService.getExistingMatchingNsi("X007", CONTEXT, 1L, CONTRACT_TYPE, referralStart, null)).thenReturn(empty());

            // When/Then
            final var appointmentCreateRequest = aContextlessAppointmentCreateRequest(referralStart, startTime, endTime, CONTRACT_TYPE, null);
            final var exception = assertThrows(BadRequestException.class,
                () -> service.createAppointment("X007", 1L, CONTEXT, appointmentCreateRequest,"false"));
            assertThat(exception.getMessage()).isEqualTo("Cannot find NSI for CRN: X007 Sentence: 1 and ContractType ACC");
        }
    }

    @Nested
    class PatchAppointments {

        @Test
        public void failsToPatchAppointmentWithANonAppointmentContactType() {
            // Given
            final var crn = "X123456";
            final var appointmentId = 123456L;
            final var jsonPatch = new JsonPatch(asList(
                new ReplaceOperation(JsonPointer.of("contactType"), valueOf(RAR_CONTACT_TYPE))));
            when(jsonPatchSupport.getAsText("/contactType", jsonPatch)).thenReturn(of(RAR_CONTACT_TYPE));

            havingContactType(true, builder -> builder.attendanceContact(false), RAR_CONTACT_TYPE);

            // When
            final var exception = assertThrows(BadRequestException.class,
                () -> service.patchAppointment(crn, appointmentId, jsonPatch));
            assertThat(exception.getMessage()).isEqualTo("contact type 'CRSAPT' is not an appointment type");
        }

        @Test
        public void failsToPatchAppointmentWithANonExistentContactType() {
            // Given
            final var crn = "X123456";
            final var appointmentId = 123456L;
            final var jsonPatch = new JsonPatch(asList(
                new ReplaceOperation(JsonPointer.of("contactType"), valueOf(RAR_CONTACT_TYPE))));
            when(jsonPatchSupport.getAsText("/contactType", jsonPatch)).thenReturn(of(RAR_CONTACT_TYPE));

            havingContactType(false, builder -> builder.attendanceContact(false), RAR_CONTACT_TYPE);

            // When
            final var exception = assertThrows(BadRequestException.class,
                () -> service.patchAppointment(crn, appointmentId, jsonPatch));
            assertThat(exception.getMessage()).isEqualTo("contact type 'CRSAPT' does not exist");
        }

        @Test
        public void patchesAppointmentWithAnAppointmentContactType() {
            // Given
            final var crn = "X123456";
            final var appointmentId = 123456L;
            final var jsonPatch = new JsonPatch(asList(
                new ReplaceOperation(JsonPointer.of("contactType"), valueOf(RAR_CONTACT_TYPE))));
            when(jsonPatchSupport.getAsText("/contactType", jsonPatch)).thenReturn(of(RAR_CONTACT_TYPE));

            final var updatedContact = ContactDto.builder().id(3L).build();
            when(deliusApiClient.patchContact(appointmentId, jsonPatch)).thenReturn(updatedContact);

            havingContactType(true, builder -> builder.attendanceContact(true), RAR_CONTACT_TYPE);

            // When
            final var response = service.patchAppointment(crn, appointmentId, jsonPatch);

            // Then
            assertThat(response.getAppointmentId()).isEqualTo(updatedContact.getId());
            verify(deliusApiClient).patchContact(appointmentId, jsonPatch);
        }

        @Test
        public void patchesAppointmentWithoutContactTypeInPatch() {
            // Given
            final var crn = "X123456";
            final var appointmentId = 123456L;
            final var jsonPatch = new JsonPatch(emptyList());

            final var updatedContact = ContactDto.builder().id(3L).build();
            when(deliusApiClient.patchContact(appointmentId, jsonPatch)).thenReturn(updatedContact);

            // When
            final var response = service.patchAppointment(crn, appointmentId, jsonPatch);

            // Then
            assertThat(response.getAppointmentId()).isEqualTo(updatedContact.getId());
            verify(deliusApiClient).patchContact(appointmentId, jsonPatch);
        }
    }

    @Nested
    class UpdateAppointments {

        @Test
        public void updateAppointmentOutcome() {
            // Given
            final var crn = "X123456";
            final var appointmentId = 123456L;

            final var request = ContextlessAppointmentOutcomeRequest.builder()
                .notes("some notes")
                .attended("LATE")
                .notifyPPOfAttendanceBehaviour(false)
                .build();
            final var expectedJsonPatch = "[{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}," +
                "{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"ATTC\"}]";

            final var updatedContact = ContactDto.builder().id(3L).build();
            when(deliusApiClient.patchContact(eq(appointmentId), argThat(patch -> asString(patch).equals(expectedJsonPatch))))
                .thenReturn(updatedContact);

            // When
            final var response = service.updateAppointmentOutcome(crn, appointmentId, CONTEXT, request);

            // Then
            assertThat(response.getAppointmentId()).isEqualTo(updatedContact.getId());
            verify(deliusApiClient).patchContact(eq(appointmentId), argThat(patch -> asString(patch).equals(expectedJsonPatch)));
        }
    }

    @Nested
    class relocateAppointments {

        @Test
        public void throwsExceptionAppointmentNotFound() {
            // Given
            final var appointmentId = 1L;

            when(contactRepository.findById(appointmentId)).thenReturn(Optional.empty());

            // When
            final var appointmentRelocateRequest = anAppointmentRelocateRequest("CRSLOND");
            final var exception = assertThrows(BadRequestException.class,
                () -> service.relocateAppointment("X007", appointmentId, appointmentRelocateRequest));
            assertThat(exception.getMessage()).isEqualTo("Cannot find Appointment for CRN: X007 and Appointment Id 1");
        }

        @Test
        public void patchesOfficeLocationCode() {
            // Given
            final var crn = "X123456";
            final var appointmentId = 123456L;

            when(contactRepository.findById(appointmentId)).thenReturn(of(Contact.builder().contactId(appointmentId).build()));

            final var expectedJsonPatch = "[{\"op\":\"replace\",\"path\":\"/officeLocation\",\"value\":\"CRSLOND\"}]";
            final var updatedContact = ContactDto.builder().id(appointmentId).build();
            when(deliusApiClient.patchContact(eq(appointmentId), argThat(patch -> asString(patch).equals(expectedJsonPatch))))
                .thenReturn(updatedContact);

            // When
            final var request = AppointmentRelocateRequest.builder().officeLocationCode("CRSLOND").build();
            final var response = service.relocateAppointment(crn, appointmentId, request);

            // Then
            assertThat(response.getAppointmentId()).isEqualTo(updatedContact.getId());
            verify(deliusApiClient).patchContact(eq(appointmentId), argThat(patch -> asString(patch).equals(expectedJsonPatch)));
        }
    }

    @Nested
    class replaceAppointments {

        @Test
        public void replacesAppointment() {
            // Given
            final var appointmentId = 1L;
            final var updatedStartTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var updatedEndTime = updatedStartTime.plusHours(1);

            when(contactRepository.findById(appointmentId)).thenReturn(Optional.of(Contact.builder()
                .nsi(uk.gov.justice.digital.delius.jpa.standard.entity.Nsi.builder().nsiId(NSI_ID).build())
                .event(Event.builder().eventId(EVENT_ID).build())
                .build()));

            final var deliusNewContactRequest = aDeliusReplaceContactRequest(updatedStartTime, updatedEndTime, "CRSLOND", EVENT_ID, NSI_ID, SP_INITIATED_RESCHEDULE);
            final var replacedContact = ContactDto.builder()
                .id(2L)
                .nsiId(NSI_ID)
                .eventId(EVENT_ID)
                .typeDescription("Office Visit")
                .date(updatedStartTime.toLocalDate())
                .startTime(updatedStartTime.toLocalTime())
                .endTime(updatedEndTime.toLocalTime())
                .officeLocation("CRSLOND")
                .build();
            when(deliusApiClient.replaceContact(appointmentId, deliusNewContactRequest)).thenReturn(replacedContact);

            // When
            final var appointmentRescheduleRequest = aContextlessAppointmentRescheduleRequest(updatedStartTime, updatedEndTime, "CRSLOND", true);
            final var response = service.rescheduleAppointment("X007", appointmentId, CONTEXT, appointmentRescheduleRequest);

            // Then
            assertThat(response.getAppointmentId()).isEqualTo(2L);
        }

        @Test
        public void replacesAppointmentWithoutLocation() {
            // Given
            final var appointmentId = 1L;
            final var updatedStartTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var updatedEndTime = updatedStartTime.plusHours(1);

            when(contactRepository.findById(appointmentId)).thenReturn(Optional.of(Contact.builder()
                .nsi(uk.gov.justice.digital.delius.jpa.standard.entity.Nsi.builder().nsiId(NSI_ID).build())
                .event(Event.builder().eventId(EVENT_ID).build())
                .build()));

            final var deliusNewContactRequest = aDeliusReplaceContactRequest(updatedStartTime, updatedEndTime, null, EVENT_ID, NSI_ID, SP_INITIATED_RESCHEDULE);
            final var replacedContact = ContactDto.builder()
                .id(2L)
                .nsiId(NSI_ID)
                .eventId(EVENT_ID)
                .typeDescription("Office Visit")
                .date(updatedStartTime.toLocalDate())
                .startTime(updatedStartTime.toLocalTime())
                .endTime(updatedEndTime.toLocalTime())
                .officeLocation(null)
                .build();
            when(deliusApiClient.replaceContact(appointmentId, deliusNewContactRequest)).thenReturn(replacedContact);

            // When
            final var appointmentRescheduleRequest = aContextlessAppointmentRescheduleRequest(updatedStartTime, updatedEndTime, null, true);
            final var response = service.rescheduleAppointment("X007", appointmentId, CONTEXT, appointmentRescheduleRequest);

            // Then
            assertThat(response.getAppointmentId()).isEqualTo(2L);
        }

        @Test
        public void throwsExceptionAppointmentNotFound() {
            // Given
            final var appointmentId = 1L;
            final var updatedStartTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var updatedEndTime = updatedStartTime.plusHours(1);

            when(contactRepository.findById(appointmentId)).thenReturn(Optional.empty());

            // When
            final var appointmentRescheduleRequest = aContextlessAppointmentRescheduleRequest(updatedStartTime, updatedEndTime, "CRSLOND", false);
            final var exception = assertThrows(BadRequestException.class,
                () -> service.rescheduleAppointment("X007", appointmentId, CONTEXT, appointmentRescheduleRequest));
            assertThat(exception.getMessage()).isEqualTo("Cannot find Appointment for CRN: X007 and Appointment Id 1");
        }
    }

    @Test
    public void gettingAllAppointmentTypes() {
        final var types = List.of(
            anAppointmentContactType(1, Y, true, true),
            anAppointmentContactType(2, B, true, false),
            anAppointmentContactType(3, N, false, false)
        );
        when(contactTypeRepository.findAllSelectableAppointmentTypes()).thenReturn(types);

        final var observed = service.getAllAppointmentTypes();

        assertThat(observed).extracting(AppointmentType::getContactType).containsOnly("T1", "T2", "T3");
        assertThat(observed).extracting(AppointmentType::getDescription).containsOnly("D1", "D2", "D3");
        assertThat(observed).extracting(AppointmentType::getRequiresLocation)
            .containsOnly(RequiredOptional.REQUIRED, RequiredOptional.OPTIONAL, RequiredOptional.NOT_REQUIRED);

        //noinspection unchecked
        assertThat(observed).extracting(AppointmentType::getOrderTypes)
            .containsOnly(
                List.of(OrderType.CJA, OrderType.LEGACY),
                List.of(OrderType.CJA),
                List.of()
            );
    }

    private void havingContactType(boolean having, UnaryOperator<ContactTypeBuilder> builderOperator, String contactTypeAsString) {
        final var contactType = builderOperator.apply(ContactType.builder()).build();
        final Optional<ContactType> result = having ? of(contactType) : Optional.empty();
        when(contactTypeRepository.findByCode(contactTypeAsString)).thenReturn(result);
    }

    private Nsi aNsiWithRARRequirement() {
        return Nsi.builder().nsiId(NSI_ID).requirement(
            Requirement.builder().requirementTypeMainCategory(
                KeyValue.builder().code(RAR_TYPE_CODE).build()
            ).build()
        ).build();
    }

    private String asString(JsonPatch patch) {
        try {
            return objectMapper.writeValueAsString(patch);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private NewContact aDeliusNewContactRequest(OffsetDateTime startTime, OffsetDateTime endTime, Long nsiId, Boolean sensitive) {
        return aDeliusNewContactRequest(startTime, endTime, nsiId, sensitive, null, null);
    }

    private NewContact aDeliusNewContactRequest(OffsetDateTime startTime, OffsetDateTime endTime, Long nsiId, Boolean sensitive, String outcome, String enforcement) {
        return NewContact.builder()
            .offenderCrn("X007")
            .type(RAR_CONTACT_TYPE)
            .outcome(null)
            .provider(PROVIDER_CODE)
            .team(TEAM_CODE)
            .staff(STAFF_CODE)
            .officeLocation("CRSSHEF")
            .date(toLondonLocalDate(startTime))
            .startTime(toLondonLocalTime(startTime))
            .endTime(toLondonLocalTime(endTime))
            .alert(null)
            .sensitive(sensitive)
            .rarActivity(true)
            .notes("/url")
            .description(null)
            .eventId(1L)
            .nsiId(nsiId)
            .requirementId(null)
            .outcome(outcome)
            .enforcement(enforcement)
            .build();
    }

    private ReplaceContact aDeliusReplaceContactRequest(OffsetDateTime updatedStartTime, OffsetDateTime updatedEndTime, String officeLocation, Long eventId, Long nsiId, String outcome) {
        return ReplaceContact.builder()
            .offenderCrn("X007")
            .outcome(outcome)
            .date(toLondonLocalDate(updatedStartTime))
            .startTime(toLondonLocalTime(updatedStartTime))
            .endTime(toLondonLocalTime(updatedEndTime))
            .officeLocation(officeLocation)
            .eventId(eventId)
            .nsiId(nsiId)
            .requirementId(null)
            .build();
    }

    private AppointmentCreateRequest aAppointmentCreateRequest(OffsetDateTime startTime, OffsetDateTime endTime, Long nsiId, boolean sensitive, Boolean rarActivity) {
        return AppointmentCreateRequest.builder()
            .nsiId(nsiId)
            .contactType(RAR_CONTACT_TYPE)
            .appointmentStart(startTime)
            .appointmentEnd(endTime)
            .officeLocationCode("CRSSHEF")
            .notes("/url")
            .providerCode("CRS")
            .staffCode("CRSUATU")
            .teamCode("CRSUAT")
            .sensitive(sensitive)
            .rarActivity(rarActivity)
            .build();
    }

    private ContextlessAppointmentRescheduleRequest aContextlessAppointmentRescheduleRequest(OffsetDateTime updatedStartTime, OffsetDateTime updatedEndTime, String officeLocationCode, Boolean initiatedBySp) {
        return ContextlessAppointmentRescheduleRequest.builder()
            .updatedAppointmentStart(updatedStartTime)
            .updatedAppointmentEnd(updatedEndTime)
            .officeLocationCode(officeLocationCode)
            .initiatedByServiceProvider(initiatedBySp)
            .build();
    }

    private AppointmentRelocateRequest anAppointmentRelocateRequest(String officeLocationCode) {
        return AppointmentRelocateRequest.builder()
            .officeLocationCode(officeLocationCode)
            .build();
    }

    private ContextlessAppointmentCreateRequest aContextlessAppointmentCreateRequest(
        OffsetDateTime referralStart, OffsetDateTime startTime, OffsetDateTime endTime, String contractType, UUID referralId) {
        return aContextlessAppointmentCreateRequest(referralStart, startTime, endTime, contractType, referralId, null, null);
    }

    private ContextlessAppointmentCreateRequest aContextlessAppointmentCreateRequest(
        OffsetDateTime referralStart, OffsetDateTime startTime, OffsetDateTime endTime, String contractType, UUID referralId, String attended, Boolean notifyPP) {
        return ContextlessAppointmentCreateRequest.builder()
            .contractType(contractType)
            .referralStart(referralStart)
            .referralId(referralId)
            .appointmentStart(startTime)
            .appointmentEnd(endTime)
            .officeLocationCode("CRSSHEF")
            .notes("/url")
            .countsTowardsRarDays(true)
            .attended(attended)
            .notifyPPOfAttendanceBehaviour(notifyPP)
            .build();
    }

    private static ContactType anAppointmentContactType(int id, YesNoBlank locationFlag, boolean cja, boolean legacy) {
        return ContactType.builder()
            .code(String.format("T%d", id))
            .description(String.format("D%d", id))
            .locationFlag(locationFlag)
            .cjaOrderLevel(cja ? "Y" : "N")
            .legacyOrderLevel(legacy ? "Y" : "N")
            .build();
    }
}

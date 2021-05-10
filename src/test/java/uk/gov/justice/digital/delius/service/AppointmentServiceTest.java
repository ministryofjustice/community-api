package uk.gov.justice.digital.delius.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentType;
import uk.gov.justice.digital.delius.data.api.AppointmentType.OrderType;
import uk.gov.justice.digital.delius.data.api.AppointmentType.RequiredOptional;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;
import uk.gov.justice.digital.delius.data.api.Requirement;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType.ContactTypeBuilder;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.utils.JsonPatchSupport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.fasterxml.jackson.databind.node.TextNode.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalTime;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {
    private static final String PROVIDER_CODE = "CRS";
    private static final String STAFF_CODE = "CRSUATU";
    private static final String TEAM_CODE = "CRSUAT";
    private static final String RAR_TYPE_CODE = "F";
    private static final String CRSAPT_CONTACT_TYPE = "CRSAPT";
    private static final String CONTEXT = "commissioned-rehabilitation-services";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private RequirementService requirementService;
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

    private IntegrationContext integrationContext;

    @BeforeEach
    public void before(){
        final var integrationContextConfig = new DeliusIntegrationContextConfig();
        integrationContext = new IntegrationContext();
        integrationContextConfig.getIntegrationContexts().put(CONTEXT, integrationContext);
        integrationContext.setProviderCode(PROVIDER_CODE);
        integrationContext.setStaffCode(STAFF_CODE);
        integrationContext.setTeamCode(TEAM_CODE);
        integrationContext.setRequirementRehabilitationActivityType(RAR_TYPE_CODE);
        integrationContext.getContactMapping().setAppointmentContactType(CRSAPT_CONTACT_TYPE);
        integrationContext.getContactMapping().setAttendanceAndBehaviourNotifiedMappingToOutcomeType(
            new HashMap<>() {{
                this.put("late", new HashMap<>() {{
                    this.put(false, "ATTC");
                }});
            }}
        );

        service = new AppointmentService(contactTypeRepository, contactRepository, requirementService, deliusApiClient,
            integrationContextConfig, jsonPatchSupport);
    }

    @Test
    public void appointmentsSortedByContactDateDescending() {
        when(contactRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(ImmutableList.of());
        service.appointmentsFor(1L, AppointmentFilter.builder().build());

        verify(contactRepository).findAll(specificationArgumentCaptor.capture(), sortArgumentCaptor.capture());

        assertThat(sortArgumentCaptor.getValue().getOrderFor("contactDate")).isNotNull();
        assertThat(sortArgumentCaptor.getValue().getOrderFor("contactDate").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    public void createsAppointment() {
        // Given
        final var startTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final var endTime = startTime.plusHours(1);

        havingContactType(true, builder -> builder.attendanceContact("Y"));

        final var deliusNewContactRequest = aDeliusNewContactRequest(startTime, endTime);
        final var createdContact = ContactDto.builder().id(3L).typeDescription("Office Visit")
            .date(LocalDate.of(2021, 1, 31))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(11, 0))
            .build();
        when(deliusApiClient.createNewContact(deliusNewContactRequest)).thenReturn(createdContact);

        // When
        final var appointmentCreateRequest = aAppointmentCreateRequest(startTime, endTime);
        final var response = service.createAppointment("X007", 1L, appointmentCreateRequest);

        // Then
        assertThat(response.getAppointmentId()).isEqualTo(3L);
        verifyNoInteractions(requirementService);
    }

    @Test
    public void failsToCreateAppointmentWithMissingContactType() {
        // Given
        final var startTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final var endTime = startTime.plusHours(1);

        havingContactType(false, builder -> builder);

        // When
        final var appointmentCreateRequest = aAppointmentCreateRequest(startTime, endTime);
        assertThrows(BadRequestException.class,
            () -> service.createAppointment("X007", 1L, appointmentCreateRequest),
            "contact type 'X007' does not exist");
    }

    @Test
    public void failsToCreateAppointmentWithNonAppointmentContactType() {
        // Given
        final var startTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final var endTime = startTime.plusHours(1);

        havingContactType(true, builder -> builder.attendanceContact("N"));

        // When
        final var appointmentCreateRequest = aAppointmentCreateRequest(startTime, endTime);
        assertThrows(BadRequestException.class,
            () -> service.createAppointment("X007", 1L, appointmentCreateRequest),
            "contact type 'X007' is not an appointment type");
    }

    @Test
    public void createsAppointmentUsingContextlessClientRequest() {
        // Given
        final var requirement = Requirement.builder().requirementId(99L).build();
        when(requirementService.getRequirement("X007", 1L, RAR_TYPE_CODE)).thenReturn(requirement);

        final var startTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final var endTime = startTime.plusHours(1);

        havingContactType(true, builder -> builder.attendanceContact("Y"));

        final var deliusNewContactRequest = aDeliusNewContactRequest(startTime, endTime);
        final var createdContact = ContactDto.builder().id(3L)
            .date(LocalDate.of(2021, 1, 31))
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(11, 0))
            .build();
        when(deliusApiClient.createNewContact(deliusNewContactRequest)).thenReturn(createdContact);

        // When
        final var appointmentCreateRequest = aContextlessAppointmentCreateRequest(startTime, endTime);
        final var response = service.createAppointment("X007", 1L, CONTEXT, appointmentCreateRequest);

        // Then
        assertThat(response.getAppointmentId()).isEqualTo(3L);
    }

    @Test
    public void failsToPatchAppointmentWithANonAppointmentContactType() {
        // Given
        final var crn = "X123456";
        final var appointmentId = 123456L;
        final var jsonPatch = new JsonPatch(asList(
            new ReplaceOperation(JsonPointer.of("contactType"), valueOf(CRSAPT_CONTACT_TYPE))));
        when(jsonPatchSupport.getAsText("/contactType", jsonPatch)).thenReturn(of(CRSAPT_CONTACT_TYPE));

        havingContactType(true, builder -> builder.attendanceContact("N"));

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
            new ReplaceOperation(JsonPointer.of("contactType"), valueOf(CRSAPT_CONTACT_TYPE))));
        when(jsonPatchSupport.getAsText("/contactType", jsonPatch)).thenReturn(of(CRSAPT_CONTACT_TYPE));

        havingContactType(false, builder -> builder.attendanceContact("N"));

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
            new ReplaceOperation(JsonPointer.of("contactType"), valueOf(CRSAPT_CONTACT_TYPE))));
        when(jsonPatchSupport.getAsText("/contactType", jsonPatch)).thenReturn(of(CRSAPT_CONTACT_TYPE));

        final var updatedContact = ContactDto.builder().id(3L).build();
        when(deliusApiClient.patchContact(appointmentId, jsonPatch)).thenReturn(updatedContact);

        havingContactType(true, builder -> builder.attendanceContact("Y"));

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

    @Test
    public void gettingAllAppointmentTypes() {
        final var types = List.of(
            anAppointmentContactType(1, "Y", true, true),
            anAppointmentContactType(2, "B", true, false),
            anAppointmentContactType(3, "N", false, false)
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

    private void havingContactType(boolean having, UnaryOperator<ContactTypeBuilder> builderOperator) {
        final var contactType = builderOperator.apply(ContactType.builder()).build();
        final Optional<ContactType> result = having ? of(contactType) : Optional.empty();
        when(contactTypeRepository.findByCode(CRSAPT_CONTACT_TYPE)).thenReturn(result);
    }

    private String asString(JsonPatch patch) {
        try {
            return objectMapper.writeValueAsString(patch);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private NewContact aDeliusNewContactRequest(OffsetDateTime startTime, OffsetDateTime endTime) {
        return NewContact.builder()
            .offenderCrn("X007")
            .type(CRSAPT_CONTACT_TYPE)
            .outcome(null)
            .provider(PROVIDER_CODE)
            .team(TEAM_CODE)
            .staff(STAFF_CODE)
            .officeLocation("CRSSHEF")
            .date(toLondonLocalDate(startTime))
            .startTime(toLondonLocalTime(startTime))
            .endTime(toLondonLocalTime(endTime))
            .alert(null)
            .sensitive(null)
            .notes("/url")
            .description(null)
            .eventId(1L)
            .requirementId(99L)
            .build();
    }

    private AppointmentCreateRequest aAppointmentCreateRequest(OffsetDateTime startTime, OffsetDateTime endTime) {
        return AppointmentCreateRequest.builder()
            .requirementId(99L)
            .contactType(CRSAPT_CONTACT_TYPE)
            .appointmentStart(startTime)
            .appointmentEnd(endTime)
            .officeLocationCode("CRSSHEF")
            .notes("/url")
            .providerCode("CRS")
            .staffCode("CRSUATU")
            .teamCode("CRSUAT")
            .build();
    }

    private ContextlessAppointmentCreateRequest aContextlessAppointmentCreateRequest(OffsetDateTime startTime, OffsetDateTime endTime) {
        return ContextlessAppointmentCreateRequest.builder()
            .appointmentStart(startTime)
            .appointmentEnd(endTime)
            .officeLocationCode("CRSSHEF")
            .notes("/url")
            .build();
    }

    private static ContactType anAppointmentContactType(int id, String locationFlag, boolean cja, boolean legacy) {
        return ContactType.builder()
            .code(String.format("T%d", id))
            .description(String.format("D%d", id))
            .locationFlag(locationFlag)
            .cjaOrderLevel(cja ? "Y" : "N")
            .legacyOrderLevel(legacy ? "Y" : "N")
            .build();
    }
}

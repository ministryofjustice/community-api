package uk.gov.justice.digital.delius.service;

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
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateResponse;
import uk.gov.justice.digital.delius.data.api.Requirement;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {
    private static final String PROVIDER_CODE = "CRS";
    private static final String STAFF_CODE = "CRSUATU";
    private static final String TEAM_CODE = "CRSUAT";
    private static final String RAR_TYPE_CODE = "F";
    private static final String CRSAPT_CONTACT_TYPE = "CRSAPT";
    private static final String CONTEXT = "commissioned-rehabilitation-services";

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private RequirementService requirementService;
    @Mock
    private DeliusApiClient deliusApiClient;

    @Captor
    private ArgumentCaptor<Specification<Contact>> specificationArgumentCaptor;
    @Captor
    private ArgumentCaptor<Sort> sortArgumentCaptor;
    private AppointmentService service;

    @BeforeEach
    public void before(){
        DeliusIntegrationContextConfig integrationContextConfig = new DeliusIntegrationContextConfig();
        IntegrationContext integrationContext = new IntegrationContext();
        integrationContextConfig.getIntegrationContexts().put(CONTEXT, integrationContext);
        integrationContext.setProviderCode(PROVIDER_CODE);
        integrationContext.setStaffCode(STAFF_CODE);
        integrationContext.setTeamCode(TEAM_CODE);
        integrationContext.setRequirementRehabilitationActivityType(RAR_TYPE_CODE);
        integrationContext.getContactMapping().setAppointmentContactType(CRSAPT_CONTACT_TYPE);

        service = new AppointmentService(contactRepository, requirementService, deliusApiClient, integrationContextConfig);
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
        Requirement requirement = Requirement.builder().requirementId(99L).build();
        when(requirementService.getRequirement("X007", 1L, RAR_TYPE_CODE)).thenReturn(requirement);

        var startTime = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
        var endTime = startTime.plusHours(1);

        NewContact deliusNewContactRequest = aDeliusNewContactRequest(startTime, endTime);
        ContactDto createdContact = ContactDto.builder().id(3L).build();
        when(deliusApiClient.createNewContract(deliusNewContactRequest)).thenReturn(createdContact);

        // When
        AppointmentCreateRequest appointmentCreateRequest = aAppointmentCreateRequest(startTime, endTime);
        AppointmentCreateResponse response = service.createAppointment("X007", 1L, appointmentCreateRequest);

        // Then
        assertThat(response.getAppointmentId()).isEqualTo(3L);
    }

    private NewContact aDeliusNewContactRequest(LocalTime startTime, LocalTime endTime) {
        NewContact deliusNewContactRequest = NewContact.builder()
            .offenderCrn("X007")
            .type(CRSAPT_CONTACT_TYPE)
            .outcome(null)
            .provider(PROVIDER_CODE)
            .team(TEAM_CODE)
            .staff(STAFF_CODE)
            .officeLocation("CRSSHEF")
            .date(LocalDate.now())
            .startTime(startTime)
            .endTime(endTime)
            .alert(null)
            .sensitive(null)
            .notes("/url")
            .description(null)
            .eventId(1L)
            .requirementId(99L)
            .build();
        return deliusNewContactRequest;
    }

    private AppointmentCreateRequest aAppointmentCreateRequest(LocalTime startTime, LocalTime endTime) {
        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
            .appointmentDate(LocalDate.now())
            .appointmentStartTime(startTime)
            .appointmentEndTime(endTime)
            .officeLocationCode("CRSSHEF")
            .notes("/url")
            .context(CONTEXT)
            .build();
        return request;
    }
}

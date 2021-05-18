package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.ContextlessNotificationCreateRequest;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalTime;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String CONTRACT_TYPE = "ACC";
    private static final String PROVIDER_CODE = "CRS";
    private static final String STAFF_CODE = "CRSUATU";
    private static final String TEAM_CODE = "CRSUAT";
    private static final String RAR_TYPE_CODE = "F";
    private static final String NOTIFICATION_CONTACT_TYPE = "CRS01";
    private static final String CONTEXT = "commissioned-rehabilitation-services";

    @Mock
    private ContactTypeRepository contactTypeRepository;
    @Mock
    private ReferralService referralService;
    @Mock
    private DeliusApiClient deliusApiClient;

    private NotificationService service;

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
        integrationContext.getContactMapping().setNotificationContactType(NOTIFICATION_CONTACT_TYPE);

        service = new NotificationService(contactTypeRepository, referralService, deliusApiClient, integrationContextConfig);
    }

    @Nested
    class NotifyContact {

        @Test
        public void notifiesContact() {
            // Given
            final var referralStart = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var contactDateTime = referralStart.plusHours(1);

            when(referralService.getExistingMatchingNsi("X007", CONTEXT, 100L, CONTRACT_TYPE, referralStart))
                .thenReturn(Optional.of(Nsi.builder().nsiId(99L).build()));
            when(contactTypeRepository.findByCode(NOTIFICATION_CONTACT_TYPE))
                .thenReturn(Optional.of(ContactType.builder().code(NOTIFICATION_CONTACT_TYPE).attendanceContact("N").build()));

            final var deliusNewContactRequest = aDeliusNewContactRequest(contactDateTime, 99L, 100L);
            final var createdContact = ContactDto.builder().id(3L).build();
            when(deliusApiClient.createNewContact(deliusNewContactRequest)).thenReturn(createdContact);

            // When
            final var appointmentCreateRequest = aContextlessNotificationCreateRequest(referralStart, contactDateTime, CONTRACT_TYPE);
            final var response = service.notifyContact("X007", 100L, CONTEXT, appointmentCreateRequest);

            // Then
            assertThat(response.getContactId()).isEqualTo(3L);
        }

        @Test
        public void failsToCreateAppointmentFromContextlessRequestWhenNsiDoesNotExist() {
            // Given
            final var referralStart = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var contactDateTime = referralStart.plusHours(1);

            when(referralService.getExistingMatchingNsi("X007", CONTEXT, 100L, CONTRACT_TYPE, referralStart))
                .thenReturn(Optional.empty());

            // When/Then
            final var appointmentCreateRequest = aContextlessNotificationCreateRequest(referralStart, contactDateTime, CONTRACT_TYPE);
            final var exception = assertThrows(BadRequestException.class,
                () -> service.notifyContact("X007", 100L, CONTEXT, appointmentCreateRequest));
            assertThat(exception.getMessage()).isEqualTo("Cannot find NSI for CRN: X007 Sentence: 100 and ContractType ACC");
        }
    }

    @Nested
    class ValidateContactType {

        @Test
        public void mustNotBeAppointmentType() {
            // Given
            final var referralStart = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var contactDateTime = referralStart.plusHours(1);

            when(referralService.getExistingMatchingNsi("X007", CONTEXT, 100L, CONTRACT_TYPE, referralStart))
                .thenReturn(Optional.of(Nsi.builder().nsiId(99L).build()));
            when(contactTypeRepository.findByCode(NOTIFICATION_CONTACT_TYPE))
                .thenReturn(Optional.of(ContactType.builder().code(NOTIFICATION_CONTACT_TYPE).attendanceContact("Y").build()));

            // When
            final var appointmentCreateRequest = aContextlessNotificationCreateRequest(referralStart, contactDateTime, CONTRACT_TYPE);
            final var exception = assertThrows(BadRequestException.class,
                () -> service.notifyContact("X007", 100L, CONTEXT, appointmentCreateRequest));
            assertThat(exception.getMessage()).isEqualTo("contact type 'CRS01' must not be an appointment type");
        }

        @Test
        public void catchesUnknownContactType() {
            // Given
            final var referralStart = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            final var contactDateTime = referralStart.plusHours(1);

            when(referralService.getExistingMatchingNsi("X007", CONTEXT, 100L, CONTRACT_TYPE, referralStart))
                .thenReturn(Optional.of(Nsi.builder().nsiId(99L).build()));
            when(contactTypeRepository.findByCode(NOTIFICATION_CONTACT_TYPE))
                .thenReturn(Optional.empty());

            // When
            final var appointmentCreateRequest = aContextlessNotificationCreateRequest(referralStart, contactDateTime, CONTRACT_TYPE);
            final var exception = assertThrows(BadRequestException.class,
                () -> service.notifyContact("X007", 100L, CONTEXT, appointmentCreateRequest));
            assertThat(exception.getMessage()).isEqualTo("contact type 'CRS01' does not exist");
        }
    }

    private NewContact aDeliusNewContactRequest(OffsetDateTime contactDateTime, Long nsiId, Long sentenceId) {
        return NewContact.builder()
            .offenderCrn("X007")
            .type(NOTIFICATION_CONTACT_TYPE)
            .outcome(null)
            .provider(PROVIDER_CODE)
            .team(TEAM_CODE)
            .staff(STAFF_CODE)
            .date(toLondonLocalDate(contactDateTime))
            .startTime(toLondonLocalTime(contactDateTime))
            .notes("/url")
            .eventId(sentenceId)
            .nsiId(nsiId)
            .build();
    }
    private ContextlessNotificationCreateRequest aContextlessNotificationCreateRequest(
        OffsetDateTime referralStart, OffsetDateTime contactDateTime, String contractType) {
        return ContextlessNotificationCreateRequest.builder()
            .contractType(contractType)
            .referralStart(referralStart)
            .contactDateTime(contactDateTime)
            .notes("/url")
            .build();
    }

}
package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.ContextlessNotificationCreateRequest;
import uk.gov.justice.digital.delius.data.api.NotificationCreateRequest;
import uk.gov.justice.digital.delius.data.api.NotificationResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.justice.digital.delius.transformers.NotificationCreateRequestTransformer.notificationOf;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDate;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalTime;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationService {

    private final OffenderRepository offenderRepository;
    private final ContactTypeRepository contactTypeRepository;
    private final ContactRepository contactRepository;
    private final ReferralService referralService;
    private final DeliusApiClient deliusApiClient;
    private final DeliusIntegrationContextConfig deliusIntegrationContextConfig;

    @Transactional
    public NotificationResponse notifyContact(final String crn, final Long sentenceId, final NotificationCreateRequest request) {

        assertAppointmentType(request.getContactType());

        final var newContact = makeNewCRSContact(crn, sentenceId, request);
        final var contactDto = deliusApiClient.createNewContact(newContact);
        return NotificationResponse.builder().contactId(contactDto.getId()).build();
    }

    @Transactional
    public NotificationResponse notifyCRSContact(final String crn, final Long sentenceId, final String contextName, final ContextlessNotificationCreateRequest contextlessRequest) {

        assertCRSContext(contextName);

        final var context = getContext(contextName);
        final var request = referralService.getExistingMatchingNsi(crn, contextName, sentenceId, contextlessRequest.getContractType(), contextlessRequest.getReferralStart(), contextlessRequest.getReferralId())
            .map(nsi -> notificationOf(contextlessRequest, nsi, context))
            .orElseThrow(() -> new BadRequestException(format("Cannot find NSI for CRN: %s Sentence: %d and ContractType %s", crn, sentenceId, contextlessRequest.getContractType())));

        return matchExistingCRSContact(crn, request)
            .map(contactId -> NotificationResponse.builder().contactId(contactId).build())
            .orElseGet(() -> notifyContact(crn, sentenceId, request));
    }

    private void assertAppointmentType(String contactTypeCode) {
        final var type = this.contactTypeRepository.findByCode(contactTypeCode)
            .orElseThrow(() -> new BadRequestException(format("contact type '%s' does not exist", contactTypeCode)));

        if (type.getAttendanceContact()) {
            throw new BadRequestException(format("contact type '%s' must not be an appointment type", contactTypeCode));
        }
    }

    private void assertCRSContext(String contextName) {
        if (!"commissioned-rehabilitation-services".equals(contextName)) {
            throw new BadRequestException(format("Service called with invalid CRS context %s", contextName));
        }
    }

    private Optional<Long> matchExistingCRSContact(String crn, NotificationCreateRequest request) {
        Offender offender = offenderRepository.findByCrn(crn).orElseThrow(() -> new BadRequestException(format("Cannot find offender with CRN: %s", crn)));
        var contactLocalDate = toLondonLocalDate(request.getContactDateTime());
        var contactStartLocalTime = toLondonLocalTime(request.getContactDateTime()).truncatedTo(ChronoUnit.SECONDS);

        return contactRepository.findByOffenderAndNsiIdAndContactTypeAndContactDateAndSoftDeletedIsFalse(
                offender.getOffenderId(),
                request.getNsiId(),
                request.getContactType(),
                contactLocalDate)
            .stream()
            .filter(contact -> contactStartLocalTime.equals(contact.getContactStartTime()))
            .findFirst()
            .map(Contact::getContactId);
    }

    private NewContact makeNewCRSContact(String crn, Long sentenceId, NotificationCreateRequest request) {
        return NewContact.builder()
            .offenderCrn(crn)
            .type(request.getContactType())
            .date(toLondonLocalDate(request.getContactDateTime()))
            .startTime(toLondonLocalTime(request.getContactDateTime()))
            .provider(request.getProviderCode())
            .team(request.getTeamCode())
            .staff(request.getStaffCode())
            .notes(request.getNotes())
            .nsiId(request.getNsiId())
            .eventId(sentenceId)
            .requirementId(request.getRequirementId())
            .build();
    }

    IntegrationContext getContext(String name) {
        var context = deliusIntegrationContextConfig.getIntegrationContexts().get(name);
        return Optional.ofNullable(context).orElseThrow(
            () -> new IllegalArgumentException("IntegrationContext does not exist for: " + name)
        );
    }

}

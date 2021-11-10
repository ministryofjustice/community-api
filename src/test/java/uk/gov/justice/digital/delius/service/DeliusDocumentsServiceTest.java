package uk.gov.justice.digital.delius.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.data.api.Team;
import uk.gov.justice.digital.delius.data.api.UploadedDocumentCreateResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.UploadedDocumentDto;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DeliusDocumentsServiceTest {

    @Mock
    private DeliusApiClient deliusApiClient;
    @Mock
    private OffenderManagerService offenderManagerService;
    @Captor
    private ArgumentCaptor<NewContact> newContactArgumentCaptor;

    private DeliusDocumentsService deliusDocumentsService;

    private final long eventId = 9849L;
    private final String crn = "X1923";
    private final long contactId = 123L;
    private final String EASU = "EASU";
    private final String authorName = "Author Name";
    private final String documentName = "Document Name";
    private final LocalDateTime now = LocalDateTime.now();
    private final ContactType contactType = new ContactType();
    private final MultipartFile file = multiPartFile();

    @BeforeEach
    private void setup() {
        deliusDocumentsService = new DeliusDocumentsService(deliusApiClient, offenderManagerService);
        contactType.setCode(EASU);
    }

    @Test
    public void shouldCreateANewDocumentInDelius() {

        when(offenderManagerService.getAllOffenderManagersForCrn(crn, true)).thenReturn(offenderManagers());
        when(deliusApiClient.createNewContact(newContactArgumentCaptor.capture())).thenReturn(contactDto());
        when(deliusApiClient.uploadDocument(crn, contactId, file)).thenReturn(
            UploadedDocumentDto.builder()
                .crn(crn)
                .author(authorName)
                .documentName(documentName)
                .dateLastModified(now)
                .lastModifiedUser(authorName)
                .creationDate(now)
                .build()
        );

        UploadedDocumentCreateResponse response = deliusDocumentsService.createUPWDocument(crn, eventId, file);

        assertThat(response.getCrn()).isEqualTo(crn);
        assertThat(response.getDocumentName()).isEqualTo(documentName);
        assertThat(response.getDateLastModified()).isEqualTo(now);
        assertThat(response.getLastModifiedUser()).isEqualTo(authorName);
        assertThat(response.getCreationDate()).isEqualTo(now);
    }

    @Test
    public void shouldCreateContactWithActiveOffenderManager() {

        when(offenderManagerService.getAllOffenderManagersForCrn(crn, true)).thenReturn(offenderManagers());
        when(deliusApiClient.createNewContact(newContactArgumentCaptor.capture())).thenReturn(contactDto());
        when(deliusApiClient.uploadDocument(crn, contactId, file)).thenReturn(
            UploadedDocumentDto.builder()
                .crn(crn)
                .author(authorName)
                .documentName(documentName)
                .dateLastModified(now)
                .lastModifiedUser(authorName)
                .creationDate(now)
                .build()
        );

        deliusDocumentsService.createUPWDocument(crn, eventId, file);

        NewContact newContact = newContactArgumentCaptor.getValue();
        assertThat(newContact.getType()).isEqualTo(EASU);
        assertThat(newContact.getProvider()).isEqualTo("2345");
        assertThat(newContact.getTeam()).isEqualTo("Team code");
        assertThat(newContact.getStaff()).isEqualTo("Staff code");
        assertThat(newContact.getDate()).isEqualTo(LocalDate.now());
        assertThat(newContact.getEventId()).isEqualTo(eventId);
    }

    @Test
    public void shouldThrowExceptionIfOffenderHasNoOffenderManagers() {
        ContactType contactType = new ContactType();
        contactType.setCode(EASU);

        when(offenderManagerService.getAllOffenderManagersForCrn(crn, true)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class, ()
            -> deliusDocumentsService.createUPWDocument(crn, eventId, file));

        assertThat(exception.getMessage()).isEqualTo("Offender Managers not found for crn %s", crn);
    }

    @Test
    public void shouldThrowExceptionIfOffenderHasNoOffenderActiveManager() {
        ContactType contactType = new ContactType();
        contactType.setCode(EASU);

        when(offenderManagerService.getAllOffenderManagersForCrn(crn, true)).thenReturn(
            Optional.of(List.of(CommunityOrPrisonOffenderManager.builder().isResponsibleOfficer(false).build()))
        );

        Exception exception = assertThrows(NotFoundException.class, ()
            -> deliusDocumentsService.createUPWDocument(crn, eventId, file));

        assertThat(exception.getMessage()).isEqualTo("No active Offender Manager found for crn %s", crn);
    }

    private Optional<List<CommunityOrPrisonOffenderManager>> offenderManagers() {
        return Optional.of(
            List.of(
                CommunityOrPrisonOffenderManager
                    .builder()
                    .isResponsibleOfficer(true)
                    .probationArea(ProbationArea.builder().code("2345").build())
                    .staffCode("Staff code")
                    .team(Team.builder().code("Team code").build())
                    .build(),
                CommunityOrPrisonOffenderManager
                    .builder()
                    .isResponsibleOfficer(false)
                    .build()
            )
        );
    }
    private ContactDto contactDto() {
        return ContactDto
            .builder()
            .offenderCrn(crn)
            .eventId(eventId)
            .type(EASU)
            .id(contactId)
            .build();
    }

    private MultipartFile multiPartFile() {
        return new MockMultipartFile(
            "file",
            "filename",
            MediaType.TEXT_PLAIN_VALUE,
            "Test information contained in a document".getBytes()
        );
    }
}
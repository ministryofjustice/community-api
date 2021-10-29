package uk.gov.justice.digital.delius.service;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.UploadedDocumentCreateResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.UploadedDocumentDto;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import java.time.LocalDateTime;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class DeliusDocumentsServiceTest {

    public static final long EVENT_ID = 9849L;
    private static final String CRN = "X1923";
    private DeliusDocumentsService deliusDocumentsService;
    private final MultipartFile file = multiPartFile();
    @Mock
    private DeliusApiClient deliusApiClient;
    @Mock
    private ContactTypeRepository contactTypeRepository;



    @BeforeEach
    private void setup(){
        deliusDocumentsService = new DeliusDocumentsService(deliusApiClient, contactTypeRepository);
    }

    @Test
    public void shouldThrowExceptionIfContactTypeIsNotCompletedUnpaidWork(){
        String incorrect = "INCORRECT";

        ContactType contactType = new ContactType();
        contactType.setCode(incorrect);
        when(contactTypeRepository.findByCode(incorrect)).thenReturn(Optional.of(contactType));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            deliusDocumentsService.createDocument("X1923", 9849L, incorrect, file);
        });

        assertThat(exception.getMessage()).isEqualTo(format("contact type '%s' is not a completed UPW assessment type", incorrect));
    }

    @Test
    public void shouldThrowExceptionIfContactTypeIsNotFound(){
        String invalid = "INVALID";

        when(contactTypeRepository.findByCode(invalid)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            deliusDocumentsService.createDocument("X1923", 9849L, invalid, file);
        });
        assertThat(exception.getMessage()).isEqualTo(format("contact type '%s' does not exist", invalid));
    }

    @Test
    public void testWeCanCreateANewDocumentInDelius(){
        String easu = "EASU";
        long contactId = 123L;

        ContactType contactType = new ContactType();
        contactType.setCode(easu);

        when(contactTypeRepository.findByCode(easu)).thenReturn(Optional.of(contactType));

        NewContact newContact = NewContact.builder().offenderCrn(CRN).eventId(EVENT_ID).type(easu).build();
        ContactDto contactDto = ContactDto.builder().offenderCrn(CRN).eventId(EVENT_ID).type(easu).id(contactId).build();
        when(deliusApiClient.createNewContact(newContact)).thenReturn(contactDto);

        String author_name = "Author Name";
        LocalDateTime now = LocalDateTime.now();
        String documentName = "Document Name";

        when(deliusApiClient.uploadDocument(CRN, contactId, file)).thenReturn(
            UploadedDocumentDto.builder()
                .crn(CRN)
                .author(author_name)
                .documentName(documentName)
                .dateLastModified(now)
                .lastModifiedUser(author_name)
                .creationDate(now)
                .build()
        );

        UploadedDocumentCreateResponse response = deliusDocumentsService.createDocument(CRN, EVENT_ID, easu, file);

        assertThat(response.getCrn()).isEqualTo(CRN);
        assertThat(response.getAuthor()).isEqualTo(author_name);
        assertThat(response.getDocumentName()).isEqualTo(documentName);
        assertThat(response.getDateLastModified()).isEqualTo(now);
        assertThat(response.getLastModifiedUser()).isEqualTo(author_name);
        assertThat(response.getCreationDate()).isEqualTo(now);
    }

    private MultipartFile multiPartFile(){
        return new MockMultipartFile(
            "file",
            "filename",
            MediaType.TEXT_PLAIN_VALUE,
            "Test information contained in a document".getBytes()
        );
    }
}
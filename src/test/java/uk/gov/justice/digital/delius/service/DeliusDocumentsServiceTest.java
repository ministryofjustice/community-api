package uk.gov.justice.digital.delius.service;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.UploadedDocumentCreateResponse;
import uk.gov.justice.digital.delius.data.api.deliusapi.ContactDto;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewContact;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewDocument;
import uk.gov.justice.digital.delius.data.api.deliusapi.UploadedDocumentDto;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import java.time.LocalDateTime;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class DeliusDocumentsServiceTest {

    private DeliusDocumentsService deliusDocumentsService;

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
        NewDocument newDocument = new NewDocument();
        String incorrect = "INCORRECT";

        ContactType contactType = new ContactType();
        contactType.setCode(incorrect);
        when(contactTypeRepository.findByCode(incorrect)).thenReturn(Optional.of(contactType));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            deliusDocumentsService.createDocument("X1923", 9849L, incorrect, newDocument);
        });

        assertThat(exception.getMessage()).isEqualTo(format("contact type '%s' is not a completed UPW assessment type", incorrect));
    }

    @Test
    public void shouldThrowExceptionIfContactTypeIsNotFound(){
        NewDocument newDocument = new NewDocument();
        String invalid = "INVALID";

        when(contactTypeRepository.findByCode(invalid)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            deliusDocumentsService.createDocument("X1923", 9849L, invalid, newDocument);
        });
        assertThat(exception.getMessage()).isEqualTo(format("contact type '%s' does not exist", invalid));
    }

    @Test
    public void testWeCanCreateANewDocumentInDelius(){
        String crn = "X1923";
        long eventId = 9849L;
        String easu = "EASU";
        long contactId = 123L;

        NewDocument newDocument = new NewDocument();
        ContactType contactType = new ContactType();
        contactType.setCode(easu);

        when(contactTypeRepository.findByCode(easu)).thenReturn(Optional.of(contactType));

        NewContact newContact = NewContact.builder().offenderCrn(crn).eventId(eventId).type(easu).build();
        ContactDto contactDto = ContactDto.builder().offenderCrn(crn).eventId(eventId).type(easu).id(contactId).build();
        when(deliusApiClient.createNewContact(newContact)).thenReturn(contactDto);

        String author_name = "Author Name";
        LocalDateTime now = LocalDateTime.now();
        String documentName = "Document Name";
        when(deliusApiClient.uploadDocument(crn, contactId, newDocument)).thenReturn(
            UploadedDocumentDto.builder()
                .crn(crn)
                .author(author_name)
                .documentName(documentName)
                .dateLastModified(now)
                .lastModifiedUser(author_name)
                .creationDate(now)
                .build()
        );

        UploadedDocumentCreateResponse response = deliusDocumentsService.createDocument(crn, eventId, easu, newDocument);

        assertThat(response.getCrn()).isEqualTo(crn);
        assertThat(response.getAuthor()).isEqualTo(author_name);
        assertThat(response.getDocumentName()).isEqualTo(documentName);
        assertThat(response.getDateLastModified()).isEqualTo(now);
        assertThat(response.getLastModifiedUser()).isEqualTo(author_name);
        assertThat(response.getCreationDate()).isEqualTo(now);
    }
}
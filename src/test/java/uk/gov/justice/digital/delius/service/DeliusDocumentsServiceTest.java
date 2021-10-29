package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.deliusapi.NewDocument;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;


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
    public void testWeCantCreateANewDocumentInDeliusIfContactTypeIncorrect(){
        NewDocument newDocument = new NewDocument();

        Assertions.assertThrows(BadRequestException.class, () -> {
            deliusDocumentsService.createDocument("X1923", 9849L, "C/UPW", newDocument);
        });
    }

}
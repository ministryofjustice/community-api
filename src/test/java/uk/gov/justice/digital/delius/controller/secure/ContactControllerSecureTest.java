package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactControllerSecureTest {

    @InjectMocks
    ContactControllerSecure controller;

    @Mock
    OffenderService offenderService;

    @Mock
    ContactService contactService;

    @Test
    @DisplayName("will return a contact")
    public void gettingAContact() {
        final var crn = "X123";
        final var offenderId = 20L;
        final var contactId = 10L;
        final var aContact = Contact.builder().contactId(contactId).build();

        when(offenderService.offenderIdOfCrn(crn)).thenReturn(Optional.of(offenderId));
        when(contactService.getContact(offenderId, contactId)).thenReturn(Optional.of(aContact));
        final var observed = controller.getOffenderContactByCrn(crn, contactId);
        assertThat(observed).usingRecursiveComparison().isEqualTo(aContact);
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.ContactSummary;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.OffenderService;


import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        final var aContact = ContactSummary.builder().contactId(contactId).build();

        when(offenderService.offenderIdOfCrn(crn)).thenReturn(of(offenderId));
        when(contactService.getContactSummary(offenderId, contactId)).thenReturn(of(aContact));
        final var observed = controller.getOffenderContactSummaryByCrn(crn, contactId);
        assertThat(observed).usingRecursiveComparison().isEqualTo(aContact);
    }
    @Test
    @DisplayName("will not return a contact for unknown crn")
    public void gettingContactForUnknownOffenderCrnReturnsNotFound() {
        when(offenderService.offenderIdOfCrn("notFoundCrnNumber")).thenReturn(empty());
        assertThrows(NotFoundException.class, () -> {
            controller.getOffenderContactSummaryByCrn("notFoundCrnNumber", 1234L);
        });
    }
    @Test
    @DisplayName("will not return a contact for unknown contact id")
    public void gettingContactForUnknownContactIdReturnsNotFound() {
        final var crn = "X123";
        final var offenderId = 20L;
        when(offenderService.offenderIdOfCrn(crn)).thenReturn(of(offenderId));
        when(contactService.getContactSummary(offenderId, 000L)).thenReturn(empty());
        assertThrows(NotFoundException.class, () -> {
            controller.getOffenderContactSummaryByCrn(crn, 000L);
        });
    }

}

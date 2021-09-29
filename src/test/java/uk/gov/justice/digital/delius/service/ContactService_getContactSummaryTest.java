package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactDateRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aContact;

@ExtendWith(MockitoExtension.class)
public class ContactService_getContactSummaryTest {

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ContactDateRepository contactDateRepository;
    @Mock
    private ContactTypeRepository contactTypeRepository;

    @InjectMocks
    private ContactService contactService;

    @Test
    @DisplayName("will return a contact for given contact id and offender id ")
    public void willReturnAContact() {
        final var offenderId = 20L;
        final var contactId = 10L;
        final var entityContact = aContact().toBuilder().contactId(contactId).build();
        when(contactRepository.findByContactIdAndOffenderIdAndSoftDeletedIsFalse(contactId, offenderId)).thenReturn(Optional.of(entityContact));
        final Optional<uk.gov.justice.digital.delius.data.api.ContactSummary> contactSummary = contactService.getContactSummary(offenderId, contactId);
        assertThat(contactSummary).isPresent().map(uk.gov.justice.digital.delius.data.api.ContactSummary::getContactId).hasValue(entityContact.getContactId());
    }
}

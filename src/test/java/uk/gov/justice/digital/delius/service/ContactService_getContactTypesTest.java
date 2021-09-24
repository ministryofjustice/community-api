package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactDateRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactService_getContactTypesTest {

    @InjectMocks
    private ContactService contactService;

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ContactDateRepository contactDateRepository;
    @Mock
    private ContactTypeRepository contactTypeRepository;
    @Captor
    private ArgumentCaptor<Contact> contactArgumentCaptor;

    @Test
    @DisplayName("will return all contact types for given categories")
    void willReturnAllContactTypesForGivenCategory() {
        ContactType entityContactType = aContactType("CTOB", "Phone Contact", "Phone", false);
        when(contactTypeRepository.findAllByContactCategoriesCodeValueInAndSelectableTrue(of("LT")))
            .thenReturn(of(entityContactType));
        List<uk.gov.justice.digital.delius.data.api.ContactType> observed = contactService.getContactTypes(of("LT"));
        verify(contactTypeRepository).findAllByContactCategoriesCodeValueInAndSelectableTrue(of("LT"));
        assertContactTypes(entityContactType, observed.stream().findFirst().orElse(null));
    }

    @Test
    @DisplayName("will return all contact types")
    void willReturnAllContactTypes() {
        ContactType entityContactType = aContactType("COAP", "Office Visit", "Office", true);
        when(contactTypeRepository.findAllBySelectableTrue()).thenReturn(of(entityContactType));
        List<uk.gov.justice.digital.delius.data.api.ContactType> observed = contactService.getContactTypes(emptyList());
        verify(contactTypeRepository).findAllBySelectableTrue();
        assertContactTypes(entityContactType, observed.stream().findFirst().orElse(null));
    }

    private void assertContactTypes(ContactType entityContactType, uk.gov.justice.digital.delius.data.api.ContactType observed) {
        assertThat(entityContactType.getCode()).isEqualTo(observed.getCode());
        assertThat(entityContactType.getDescription()).isEqualTo(observed.getDescription());
        assertThat(entityContactType.getShortDescription()).isEqualTo(observed.getShortDescription());
        assertThat(entityContactType.getAttendanceContact()).isEqualTo(observed.getAppointment());
    }

    private ContactType aContactType(final String code,
                                     final String description,
                                     final String shortDescription,
                                     final boolean appointment) {
        return ContactType.builder()
            .code(code)
            .description(description)
            .shortDescription(shortDescription)
            .attendanceContact(appointment)
            .build();
    }
}

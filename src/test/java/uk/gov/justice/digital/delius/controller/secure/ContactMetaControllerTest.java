package uk.gov.justice.digital.delius.controller.secure;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.ContactType;
import uk.gov.justice.digital.delius.service.ContactService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactMetaControllerTest {

    @InjectMocks
    private ContactMetaController contactMetaController;

    @Mock
    private ContactService contactService;

    @Test
    public void gettingContactTypes() {
        final var contactTypes = List.of(aContactType(), aContactType());
        when(contactService.getContactTypes(List.of("LT"))).thenReturn(contactTypes);
        final var observed = contactMetaController.getContactTypes(List.of("LT"));
        assertThat(observed).isEqualTo(contactTypes);
    }

    private ContactType aContactType() {
        return ContactType.builder().build();
    }
}

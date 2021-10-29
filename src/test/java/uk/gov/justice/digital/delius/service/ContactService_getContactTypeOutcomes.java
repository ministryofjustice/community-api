package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.ContactOutcomeTypeDetail;
import uk.gov.justice.digital.delius.data.api.RequiredOptional;
import uk.gov.justice.digital.delius.jpa.standard.YesNoBlank;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactService_getContactTypeOutcomes {

    @InjectMocks
    private ContactService contactService;

    @Mock
    private ContactTypeRepository contactTypeRepository;

    @Test
    void willReturnAvailableOutcomeDetails() {
        val entityContactType = ContactType.builder()
            .code("COAP")
            .description("Office Visit")
            .shortDescription("Office")
            .attendanceContact(true)
            .outcomeFlag(YesNoBlank.B)
            .contactOutcomeTypes(List.of(ContactOutcomeType.builder().actionRequired(true).attendance(false).code("NAT").compliantAcceptable(false).description("Not Attended").enforceable(true).build(),
                    ContactOutcomeType.builder().actionRequired(false).attendance(true).code("ATT").compliantAcceptable(true).description("Attended").enforceable(false).build()))
            .build();

        when(contactTypeRepository.findByCode(any())).thenReturn(Optional.of(entityContactType));
        var observed = contactService.getContactOutcomes("COAP");
        verify(contactTypeRepository).findByCode("COAP");

        assertThat(observed.getOutcomeRequired()).isEqualTo(RequiredOptional.OPTIONAL);
        assertThat(observed.getOutcomeTypes()).extracting(ContactOutcomeTypeDetail::getActionRequired).containsOnly(true, false);
        assertThat(observed.getOutcomeTypes()).extracting(ContactOutcomeTypeDetail::getAttendance).containsOnly(false, true);
        assertThat(observed.getOutcomeTypes()).extracting(ContactOutcomeTypeDetail::getCode).containsOnly("NAT", "ATT");
        assertThat(observed.getOutcomeTypes()).extracting(ContactOutcomeTypeDetail::getCompliantAcceptable).containsOnly(false, true);
        assertThat(observed.getOutcomeTypes()).extracting(ContactOutcomeTypeDetail::getDescription).containsOnly("Not Attended", "Attended");
        assertThat(observed.getOutcomeTypes()).extracting(ContactOutcomeTypeDetail::getEnforceable).containsOnly(true, false);
    }
}

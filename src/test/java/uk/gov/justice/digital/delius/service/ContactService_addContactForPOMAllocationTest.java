package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@RunWith(MockitoJUnitRunner.class)
public class ContactService_addContactForPOMAllocationTest {

    private ContactService contactService;

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ContactTypeRepository contactTypeRepository;
    @Captor
    private ArgumentCaptor<Contact> contactArgumentCaptor;

    @Before
    public void setup() {
        contactService = new ContactService(contactRepository, contactTypeRepository, new ContactTransformer());
        when(contactTypeRepository.findByCode(any())).thenReturn(Optional.of(aContactType()));
    }

    @Test
    public void addContactForPOMAllocationWillSetTypeOfPOMAutomatedTransfer() {
        contactService.addContactForPOMAllocation(anActivePrisonOffenderManager()
                .toBuilder()
                .allocationReason(StandardReference
                        .builder()
                        .codeValue("AUT")
                        .codeDescription("Automatic allocation")
                        .build())
                .build());

        verify(contactTypeRepository).findByCode("EPOMAT");
    }

    @Test
    public void addContactForPOMAllocationWillSetTypeOfPOMInternalTransfer() {
        contactService.addContactForPOMAllocation(anActivePrisonOffenderManager()
                .toBuilder()
                .allocationReason(StandardReference
                        .builder()
                        .codeValue("INA")
                        .codeDescription("Internal transfer")
                        .build())
                .build());

        verify(contactTypeRepository).findByCode("EPOMIN");
    }

    @Test
    public void addContactForPOMAllocationWillSetTypeOfPOMExternalTransfer() {
        contactService.addContactForPOMAllocation(anActivePrisonOffenderManager()
                .toBuilder()
                .allocationReason(StandardReference
                        .builder()
                        .codeValue("EXT")
                        .codeDescription("Extenal transfer")
                        .build())
                .build());

        verify(contactTypeRepository).findByCode("EPOMEX");
    }

    @Test
    public void addContactForPOMAllocationWillSetNotesToIncludeReasonAndDate() {
        contactService.addContactForPOMAllocation(anActivePrisonOffenderManager()
                .toBuilder()
                .allocationReason(StandardReference
                        .builder()
                        .codeValue("AUT")
                        .codeDescription("Automatic allocation")
                        .build())
                .allocationDate(LocalDate.of(2019, 7, 19))
                .build());

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes()).isEqualTo("Transfer Reason: Automatic allocation\nTransfer Date: 19/07/2019\n");
    }

    @Test
    public void addContactForPOMAllocationWillAlsoSetOldTeamInNotes() {
        contactService.addContactForPOMAllocation(
                anActivePrisonOffenderManager()
                .toBuilder()
                .allocationReason(StandardReference
                        .builder()
                        .codeValue("AUT")
                        .codeDescription("Automatic allocation")
                        .build())
                .allocationDate(LocalDate.of(2019, 7, 19))
                .build(),
                aPrisonOffenderManager(
                        aStaff().toBuilder().forename("John").forname2("Mike").surname("Smith").build(),
                        aTeam()
                                .toBuilder()
                                .description("OMU A")
                                .probationArea(aPrisonProbationArea()
                                        .toBuilder()
                                        .description("HMP Brixton")
                                        .build())
                                .build())
                    .toBuilder()
                    .build()
        );

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes())
                .isEqualTo("Transfer Reason: Automatic allocation\nTransfer Date: 19/07/2019\n\nFrom Establishment Provider: HMP Brixton\nFrom Team: OMU A\nFrom Officer: Smith, John Mike\n");
    }

    @Test
    public void addContactForPOMAllocationWillNotSetOldTeamInNotesIfTeamNotPresent() {
        contactService.addContactForPOMAllocation(
                anActivePrisonOffenderManager()
                .toBuilder()
                .allocationReason(StandardReference
                        .builder()
                        .codeValue("AUT")
                        .codeDescription("Automatic allocation")
                        .build())
                .allocationDate(LocalDate.of(2019, 7, 19))
                .build(),
                aPrisonOffenderManager(
                        aStaff().toBuilder().forename("John").forname2("Mike").surname("Smith").build(),
                null)
                    .toBuilder()
                    .build()
        );

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes())
                .isEqualTo("Transfer Reason: Automatic allocation\nTransfer Date: 19/07/2019\n");
    }

    @Test
    public void addContactForPOMAllocationWillAlsoSetUnallocatedInNotes() {
        contactService.addContactForPOMAllocation(
                anActivePrisonOffenderManager(),
                aPrisonOffenderManager(
                        aStaff().toBuilder().officerCode("NO12345U").build(),
                        aTeam())
                    .toBuilder()
                    .build()
        );

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes())
                .contains("From Officer: Unallocated");
    }

    @Test
    public void addContactForPOMAllocationWillAlsoSetInactiveInNotes() {
        contactService.addContactForPOMAllocation(
                anActivePrisonOffenderManager(),
                aPrisonOffenderManager(
                        aStaff().toBuilder().officerCode("NO12IAVU").build(),
                        aTeam())
                    .toBuilder()
                    .build()
        );

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes())
                .contains("From Officer: Inactive");
    }

}
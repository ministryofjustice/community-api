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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@RunWith(MockitoJUnitRunner.class)
public class ContactService_addContactForPrisonLocationChangeTest {

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
    public void willSetContactTypeAsPrisonChangeLocation() {
        contactService.addContactForPrisonLocationChange(anOffender(), aCustodyEvent());

        verify(contactTypeRepository).findByCode("ETCP");
    }

    @Test
    public void willSetEventOnContact() {
        final var event = aCustodyEvent();
        contactService.addContactForPrisonLocationChange(anOffender(), event);

        verify(contactRepository).save(contactArgumentCaptor.capture());
        assertThat(contactArgumentCaptor.getValue().getEvent()).isEqualTo(event);
    }



    @Test
    public void willSetNoteToIncludeCustodialStatus() {
        final var event = aCustodyEvent();
        event.getDisposal().getCustody().setCustodialStatus(StandardReference
                .builder()
                .codeValue("XX")
                .codeDescription("In Custody")
                .build());
        contactService.addContactForPrisonLocationChange(anOffender(), event);

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Custodial Status: In Custody\n");
    }

    @Test
    public void willSetNoteToIncludePrisonDescription() {
        final var event = aCustodyEvent();
        event.getDisposal().getCustody().getInstitution().setDescription("Doncaster HMP");
        contactService.addContactForPrisonLocationChange(anOffender(), event);

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Custodial Establishment: Doncaster HMP\n");
    }

    @Test
    public void willSetNoteToIncludeLocationChangeDate() {
        final var event = aCustodyEvent();
        event.getDisposal().getCustody().setLocationChangeDate(LocalDate.of(2020, 1, 31));
        contactService.addContactForPrisonLocationChange(anOffender(), event);

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Location Change Date: 2020-01-31\n");
    }

    @Test
    public void willUseFirstOrderManagerForOrganisationLinks() {
        final var team = aTeam().toBuilder().teamId(88L).build();
        final var staff = aStaff().toBuilder().staffId(77L).build();
        final var probationArea = aProbationArea();
        final var orderManager = anOrderManager()
                .toBuilder()
                .team(team)
                .staff(staff)
                .probationArea(probationArea)
                .build();
        final var event = aCustodyEvent()
                .toBuilder()
                .orderManagers(List.of(orderManager, anOrderManager()))
                .build();

        contactService.addContactForPrisonLocationChange(anOffender(), event);

        verify(contactRepository).save(contactArgumentCaptor.capture());
        assertThat(contactArgumentCaptor.getValue().getTeam()).isEqualTo(team);
        assertThat(contactArgumentCaptor.getValue().getTeamProviderId()).isEqualTo(team.getTeamId());
        assertThat(contactArgumentCaptor.getValue().getStaff()).isEqualTo(staff);
        assertThat(contactArgumentCaptor.getValue().getStaffEmployeeId()).isEqualTo(staff.getStaffId());
        assertThat(contactArgumentCaptor.getValue().getProbationArea()).isEqualTo(probationArea);
    }


}
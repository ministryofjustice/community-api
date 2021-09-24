package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactDateRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aContactType;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.aProbationArea;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOrderManager;

@ExtendWith(MockitoExtension.class)
public class ContactService_addContactForPrisonLocationChangeTest {

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

    @BeforeEach
    public void setup() {
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

        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Location Change Date: Fri Jan 31 2020\n");
    }

    @Test
    public void willUseFirstOrderManagerForOrganisationLinks() {
        final var teamForActiveOM = aTeam().toBuilder().teamId(88L).build();
        final var staffForActiveOM = aStaff().toBuilder().staffId(77L).build();
        final var teamForInactiveOM1 = aTeam().toBuilder().teamId(66L).build();
        final var staffForInactiveOM1 = aStaff().toBuilder().staffId(55L).build();
        final var teamForInactiveOM2 = aTeam().toBuilder().teamId(44L).build();
        final var staffForInactiveOM2 = aStaff().toBuilder().staffId(33L).build();
        final var probationArea = aProbationArea();
        final var activeOrderManager = anOrderManager()
                .toBuilder()
                .team(teamForActiveOM)
                .staff(staffForActiveOM)
                .probationArea(probationArea)
                .endDate(null)
                .activeFlag(1L)
                .build();
        final var inactiveOrderManager1 = anOrderManager()
                .toBuilder()
                .team(teamForInactiveOM1)
                .staff(staffForInactiveOM1)
                .probationArea(probationArea)
                .endDate(LocalDateTime.now())
                .activeFlag(0L)
                .build();
        final var inactiveOrderManager2 = anOrderManager()
                .toBuilder()
                .team(teamForInactiveOM2)
                .staff(staffForInactiveOM2)
                .probationArea(probationArea)
                .endDate(LocalDateTime.now())
                .activeFlag(0L)
                .build();
        final var event = aCustodyEvent()
                .toBuilder()
                .orderManagers(List.of(inactiveOrderManager1, activeOrderManager, inactiveOrderManager2))
                .build();

        contactService.addContactForPrisonLocationChange(anOffender(), event);

        verify(contactRepository).save(contactArgumentCaptor.capture());
        assertThat(contactArgumentCaptor.getValue().getTeam()).isEqualTo(teamForActiveOM);
        assertThat(contactArgumentCaptor.getValue().getTeamProviderId()).isEqualTo(teamForActiveOM.getTeamId());
        assertThat(contactArgumentCaptor.getValue().getStaff()).isEqualTo(staffForActiveOM);
        assertThat(contactArgumentCaptor.getValue().getStaffEmployeeId()).isEqualTo(staffForActiveOM.getStaffId());
        assertThat(contactArgumentCaptor.getValue().getProbationArea()).isEqualTo(probationArea);
    }

    @Test
    public void willSetContactStartTimeToBeNow() {
        contactService.addContactForPrisonLocationChange(anOffender(), aCustodyEvent());

        verify(contactRepository).save(contactArgumentCaptor.capture());

        final var now = LocalTime.now();
        assertThat(contactArgumentCaptor.getValue().getContactStartTime()).isBetween(now.minusMinutes(5), now);
    }
}

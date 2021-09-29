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
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactDateRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
public class ContactService_addContactForBulkCustodyKeyDateUpdateTest {

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
    public void willSetContactTypeAsDSSUpdate() {
        contactService.addContactForBulkCustodyKeyDateUpdate(anOffender(), aCustodyEvent(), Map
                .of("Parole Eligibility Date", LocalDate.of(2020, 1, 30)), Map
                .of("Sentence Expiry Date", LocalDate.of(2020, 2, 28)));

        verify(contactTypeRepository).findByCode("EDSS");
    }

    @Test
    public void willSetEventOnContact() {
        final var event = aCustodyEvent();
        contactService.addContactForBulkCustodyKeyDateUpdate(anOffender(), event, Map
                .of("Parole Eligibility Date", LocalDate.of(2020, 1, 30)), Map
                .of("Sentence Expiry Date", LocalDate.of(2020, 2, 28)));

        verify(contactRepository).save(contactArgumentCaptor.capture());
        assertThat(contactArgumentCaptor.getValue().getEvent()).isEqualTo(event);
    }



    @Test
    public void willSetDatesAndDescriptionsInNote() {
        contactService.addContactForBulkCustodyKeyDateUpdate(anOffender(), aCustodyEvent(),
                Map.of("Parole Eligibility Date", LocalDate.of(2020, 1, 30), "HDC Eligibility Date", LocalDate.of(2020, 3, 30)),
                Map.of("Sentence Expiry Date", LocalDate.of(2020, 2, 28), "Expected Release Date", LocalDate.of(2020, 5, 28)));

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Parole Eligibility Date: 30/01/2020\n");
        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("HDC Eligibility Date: 30/03/2020\n");
        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Removed Sentence Expiry Date: 28/02/2020\n");
        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Expected Release Date: 28/05/2020\n");
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

        contactService.addContactForBulkCustodyKeyDateUpdate(anOffender(), event, Map
                .of("Parole Eligibility Date", LocalDate.of(2020, 1, 30)), Map
                .of("Sentence Expiry Date", LocalDate.of(2020, 2, 28)));

        verify(contactRepository).save(contactArgumentCaptor.capture());
        assertThat(contactArgumentCaptor.getValue().getTeam()).isEqualTo(teamForActiveOM);
        assertThat(contactArgumentCaptor.getValue().getTeamProviderId()).isEqualTo(teamForActiveOM.getTeamId());
        assertThat(contactArgumentCaptor.getValue().getStaff()).isEqualTo(staffForActiveOM);
        assertThat(contactArgumentCaptor.getValue().getStaffEmployeeId()).isEqualTo(staffForActiveOM.getStaffId());
        assertThat(contactArgumentCaptor.getValue().getProbationArea()).isEqualTo(probationArea);
    }

}
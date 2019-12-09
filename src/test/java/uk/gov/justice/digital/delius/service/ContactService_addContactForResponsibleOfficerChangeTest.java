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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@RunWith(MockitoJUnitRunner.class)
public class ContactService_addContactForResponsibleOfficerChangeTest {

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
    public void addContactForResponsibleOfficerChangeWillSetResponsibleOfficerChangeType() {
        contactService.addContactForResponsibleOfficerChange(
                anActivePrisonOffenderManager(),
                anActivePrisonOffenderManager().toBuilder().staff(aStaff("A1234")).build()
        );

        verify(contactTypeRepository).findByCode("ROC");
    }

    @Test
    public void addContactForResponsibleOfficerChangeWillSetNotesToIncludeOldAndNewReasonAndDate() {
        contactService.addContactForResponsibleOfficerChange(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .allocationReason(StandardReference
                                .builder()
                                .codeValue("AUT")
                                .codeDescription("Automatic allocation")
                                .build())
                        .allocationDate(LocalDate.of(2019, 7, 19))
                        .responsibleOfficer(aResponsibleOfficer()
                                .toBuilder()
                                .startDateTime(LocalDateTime.of(2019, 7, 19, 11, 12, 59))
                                .build())
                        .build(),
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .staff(aStaff("A1234"))
                        .allocationReason(StandardReference
                                .builder()
                                .codeValue("EXT")
                                .codeDescription("External allocation")
                                .build())
                        .allocationDate(LocalDate.of(2018, 7, 19))
                        .endDate(LocalDate.of(2019, 7, 19))
                        .responsibleOfficer(aResponsibleOfficer()
                                .toBuilder()
                                .startDateTime(LocalDateTime.of(2018, 7, 19, 16, 12, 59))
                                .endDateTime(LocalDateTime.of(2019, 7, 19, 11, 12, 58))
                                .build())
                        .build()
        );

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Start Date: 19/07/2019 11:12:59\nAllocation Reason: Automatic allocation\n");
        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Start Date: 19/07/2018 16:12:59\nEnd Date: 19/07/2019 11:12:58\nAllocation Reason: External allocation\n");
    }

    @Test
    public void addContactForResponsibleOfficerChangeWillSetNotesToIncludeOldAndNewOfficeTeamArea() {
        contactService.addContactForResponsibleOfficerChange(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .staff(aStaff()
                                .toBuilder()
                                .forename("Peter")
                                .surname("Beckett")
                                .build())
                        .team(aTeam()
                                .toBuilder()
                                .description("Prison Offender Managers")
                                .build())
                        .probationArea(aPrisonProbationArea()
                                .toBuilder()
                                .description("Moorland (HMP & YOI)")
                                .build())
                        .responsibleOfficer(aResponsibleOfficer())
                        .build(),
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .staff(aStaff("A1234")
                                .toBuilder()
                                .forename("Harry")
                                .surname("Kane")
                                .build())
                        .team(aTeam()
                                .toBuilder()
                                .description("Prison Offender Managers")
                                .build())
                        .probationArea(aPrisonProbationArea()
                                .toBuilder()
                                .description("Wandsworth (HMP)")
                                .build())
                        .responsibleOfficer(aResponsibleOfficer())
                        .build()
        );

        verify(contactRepository).save(contactArgumentCaptor.capture());

        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("New Details:\n" +
                "Responsible Officer Type: Prison Offender Manager\n" +
                "Responsible Officer: Beckett, Peter  (Prison Offender Managers, Moorland (HMP & YOI))");
        assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Previous Details:\n" +
                "Responsible Officer Type: Prison Offender Manager\n" +
                "Responsible Officer: Kane, Harry  (Prison Offender Managers, Wandsworth (HMP))");
    }
}
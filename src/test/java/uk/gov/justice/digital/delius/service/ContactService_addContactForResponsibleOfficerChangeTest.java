package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactTypeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aContactType;
import static uk.gov.justice.digital.delius.util.EntityHelper.aPrisonProbationArea;
import static uk.gov.justice.digital.delius.util.EntityHelper.aResponsibleOfficer;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;
import static uk.gov.justice.digital.delius.util.EntityHelper.anActiveOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.anActivePrisonOffenderManager;

@ExtendWith(MockitoExtension.class)
public class ContactService_addContactForResponsibleOfficerChangeTest {

    private ContactService contactService;

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ContactTypeRepository contactTypeRepository;
    @Captor
    private ArgumentCaptor<Contact> contactArgumentCaptor;

    @BeforeEach
    public void setup() {
        contactService = new ContactService(contactRepository, contactTypeRepository);
        when(contactTypeRepository.findByCode(any())).thenReturn(Optional.of(aContactType()));
    }

    @Nested
    class FromPOMToPOM {
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
                            .responsibleOfficers(List.of(aResponsibleOfficer()
                                    .toBuilder()
                                    .startDateTime(LocalDateTime.of(2019, 7, 19, 11, 12, 59))
                                    .build()))
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
                            .responsibleOfficers(List.of(aResponsibleOfficer()
                                    .toBuilder()
                                    .startDateTime(LocalDateTime.of(2018, 7, 19, 16, 12, 59))
                                    .endDateTime(LocalDateTime.of(2019, 7, 19, 11, 12, 58))
                                    .build()))
                            .build()
            );

            verify(contactRepository).save(contactArgumentCaptor.capture());

            assertThat(contactArgumentCaptor.getValue().getNotes())
                    .contains("Start Date: 19/07/2019 11:12:59\nAllocation Reason: Automatic allocation\n");
            assertThat(contactArgumentCaptor.getValue().getNotes())
                    .contains("Start Date: 19/07/2018 16:12:59\nEnd Date: 19/07/2019 11:12:58\nAllocation Reason: External allocation\n");
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
                            .responsibleOfficers(List.of(aResponsibleOfficer()))
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
                            .responsibleOfficers(List.of(aResponsibleOfficer()))
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
    @Nested
    class FromPOMToCOM {
        @Test
        public void addContactForResponsibleOfficerChangeWillSetResponsibleOfficerChangeType() {
            contactService.addContactForResponsibleOfficerChange(
                    anActivePrisonOffenderManager(),
                    anActiveOffenderManager()
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
                            .responsibleOfficers(List.of(aResponsibleOfficer()
                                    .toBuilder()
                                    .startDateTime(LocalDateTime.of(2019, 7, 19, 11, 12, 59))
                                    .build()))
                            .build(),
                    anActiveOffenderManager()
                            .toBuilder()
                            .staff(aStaff("A1234"))
                            .allocationReason(StandardReference
                                    .builder()
                                    .codeValue("EXT")
                                    .codeDescription("External allocation")
                                    .build())
                            .allocationDate(LocalDate.of(2018, 7, 19))
                            .endDate(LocalDate.of(2019, 7, 19))
                            .responsibleOfficers(List.of(aResponsibleOfficer()
                                    .toBuilder()
                                    .startDateTime(LocalDateTime.of(2018, 7, 19, 16, 12, 59))
                                    .endDateTime(LocalDateTime.of(2019, 7, 19, 11, 12, 58))
                                    .build()))
                            .build()
            );

            verify(contactRepository).save(contactArgumentCaptor.capture());

            assertThat(contactArgumentCaptor.getValue().getNotes())
                    .contains("Start Date: 19/07/2019 11:12:59\nAllocation Reason: Automatic allocation\n");
            assertThat(contactArgumentCaptor.getValue().getNotes())
                    .contains("Start Date: 19/07/2018 16:12:59\nEnd Date: 19/07/2019 11:12:58\nAllocation Reason: External allocation\n");
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
                            .responsibleOfficers(List.of(aResponsibleOfficer()))
                            .build(),
                    anActiveOffenderManager()
                            .toBuilder()
                            .staff(aStaff("A1234")
                                    .toBuilder()
                                    .forename("Harry")
                                    .surname("Kane")
                                    .build())
                            .team(aTeam()
                                    .toBuilder()
                                    .description("OMU A")
                                    .build())
                            .probationArea(aPrisonProbationArea()
                                    .toBuilder()
                                    .description("NPS North West")
                                    .build())
                            .responsibleOfficers(List.of(aResponsibleOfficer()))
                            .build()
            );

            verify(contactRepository).save(contactArgumentCaptor.capture());

            assertThat(contactArgumentCaptor.getValue().getNotes()).contains("New Details:\n" +
                    "Responsible Officer Type: Prison Offender Manager\n" +
                    "Responsible Officer: Beckett, Peter  (Prison Offender Managers, Moorland (HMP & YOI))");
            assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Previous Details:\n" +
                    "Responsible Officer Type: Offender Manager\n" +
                    "Responsible Officer: Kane, Harry  (OMU A, NPS North West)");
        }
    }
    @Nested
    class FromCOMToPOM {
        @Test
        public void addContactForResponsibleOfficerChangeWillSetResponsibleOfficerChangeType() {
            contactService.addContactForResponsibleOfficerChange(
                    anActiveOffenderManager(),
                    anActivePrisonOffenderManager()
            );

            verify(contactTypeRepository).findByCode("ROC");
        }

        @Test
        public void addContactForResponsibleOfficerChangeWillSetNotesToIncludeOldAndNewReasonAndDate() {
            contactService.addContactForResponsibleOfficerChange(
                    anActiveOffenderManager()
                            .toBuilder()
                            .staff(aStaff("A1234"))
                            .allocationReason(StandardReference
                                    .builder()
                                    .codeValue("EXT")
                                    .codeDescription("External allocation")
                                    .build())
                            .allocationDate(LocalDate.of(2018, 7, 19))
                            .endDate(LocalDate.of(2019, 7, 19))
                            .responsibleOfficers(List.of(aResponsibleOfficer()
                                    .toBuilder()
                                    .startDateTime(LocalDateTime.of(2018, 7, 19, 16, 12, 59))
                                    .endDateTime(LocalDateTime.of(2019, 7, 19, 11, 12, 58))
                                    .build()))
                            .build(),
                    anActivePrisonOffenderManager()
                            .toBuilder()
                            .allocationReason(StandardReference
                                    .builder()
                                    .codeValue("AUT")
                                    .codeDescription("Automatic allocation")
                                    .build())
                            .allocationDate(LocalDate.of(2019, 7, 19))
                            .responsibleOfficers(List.of(aResponsibleOfficer()
                                    .toBuilder()
                                    .startDateTime(LocalDateTime.of(2019, 7, 19, 11, 12, 59))
                                    .build()))
                            .build()

            );

            verify(contactRepository).save(contactArgumentCaptor.capture());

            assertThat(contactArgumentCaptor.getValue().getNotes())
                    .contains("Start Date: 19/07/2019 11:12:59\nAllocation Reason: Automatic allocation\n");
            assertThat(contactArgumentCaptor.getValue().getNotes())
                    .contains("Start Date: 19/07/2018 16:12:59\nEnd Date: 19/07/2019 11:12:58\nAllocation Reason: External allocation\n");
        }

        @Test
        public void addContactForResponsibleOfficerChangeWillSetNotesToIncludeOldAndNewOfficeTeamArea() {
            contactService.addContactForResponsibleOfficerChange(
                    anActiveOffenderManager()
                            .toBuilder()
                            .staff(aStaff("A1234")
                                    .toBuilder()
                                    .forename("Harry")
                                    .surname("Kane")
                                    .build())
                            .team(aTeam()
                                    .toBuilder()
                                    .description("OMU A")
                                    .build())
                            .probationArea(aPrisonProbationArea()
                                    .toBuilder()
                                    .description("NPS North West")
                                    .build())
                            .responsibleOfficers(List.of(aResponsibleOfficer()))
                            .build(),
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
                            .responsibleOfficers(List.of(aResponsibleOfficer()))
                            .build()
            );

            verify(contactRepository).save(contactArgumentCaptor.capture());

            assertThat(contactArgumentCaptor.getValue().getNotes()).contains("New Details:\n" +
                    "Responsible Officer Type: Offender Manager\n" +
                    "Responsible Officer: Kane, Harry  (OMU A, NPS North West)");
            assertThat(contactArgumentCaptor.getValue().getNotes()).contains("Previous Details:\n" +
                    "Responsible Officer Type: Prison Offender Manager\n" +
                    "Responsible Officer: Beckett, Peter  (Prison Offender Managers, Moorland (HMP & YOI))");
        }
    }

}
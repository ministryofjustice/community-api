package uk.gov.justice.digital.delius.service;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Attendance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    public static final Long SOME_CONTACT_ID = 1234L;
    public static final Long SOME_OFFENDER_ID = 5435L;
    public static final Long SOME_EVENT_ID = 5435L;
    public static final String CONTACT_TYPE_CODE = "CT-CODE";
    public static final String CONTACT_TYPE_DESC = "CT Description";
    private static final String OUTCOME = "COT Description";

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    public void getAttendancesForEventEnforcementNoneAvailable() {

        when(contactRepository.findByOffenderAndEventIdEnforcement(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        assertTrue(attendanceService.getContactsForEventEnforcement(SOME_OFFENDER_ID, SOME_EVENT_ID, LocalDate.now()).isEmpty());

        verify(contactRepository).findByOffenderAndEventIdEnforcement(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    public void whenGetAttendancesForEventEnforcement_thenReturnMultipleAvailable() {

        final Contact contact = getContactEntity(SOME_CONTACT_ID, LocalDate.now(), "1", "1");
        when(contactRepository.findByOffenderAndEventIdEnforcement(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class))).thenReturn(singletonList(contact));

        // Act
        final List<Contact> contacts = attendanceService.getContactsForEventEnforcement(SOME_OFFENDER_ID, SOME_EVENT_ID, LocalDate.now());

        // Assert
        assertThat(contacts).hasSize(1);
        assertThat(contacts.get(0)).isSameAs(contact);
        verify(contactRepository).findByOffenderAndEventIdEnforcement(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    public void whenCallGetContacts_thenReturnList() {

        final LocalDate today = LocalDate.now();

        when(contactRepository.findByOffenderIdAndEventId(SOME_OFFENDER_ID, SOME_EVENT_ID, today))
            .thenReturn(Collections.emptyList());

        assertTrue(attendanceService.getContactsForEvent(SOME_OFFENDER_ID, SOME_EVENT_ID, today).isEmpty());

        verify(contactRepository).findByOffenderIdAndEventId(SOME_OFFENDER_ID, SOME_EVENT_ID, today);
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    public void attendancesFor() {
        final LocalDate attendanceDate = LocalDate.of(2000, Month.APRIL, 20);
        final Contact contact = getContactEntity(SOME_CONTACT_ID, attendanceDate, "Y", null);

        // Act
        final List<Attendance> attendances = AttendanceService.attendancesFor(singletonList(contact));

        // Assert
        assertThat(attendances.size()).isEqualTo(1);
        final Attendance attendance  = attendances.get(0);
        assertThat(attendance.getContactId()).isEqualTo(SOME_CONTACT_ID);
        assertThat(attendance.getAttendanceDate()).isEqualTo(attendanceDate);
        assertThat(attendance.isAttended()).isTrue();
        assertThat(attendance.isComplied()).isFalse();

        assertThat(attendance.getContactType().getCode()).isEqualTo(CONTACT_TYPE_CODE);
        assertThat(attendance.getContactType().getDescription()).isEqualTo(CONTACT_TYPE_DESC);

        assertThat(attendance.getOutcome()).isEqualTo(OUTCOME);
    }

    public static Contact getContactEntity(final Long contactId, final LocalDate attendanceDate, final String attended, final String complied) {
        return Contact.builder()
                .contactId(contactId)
                .contactDate(attendanceDate)
                .attended(attended)
                .complied(complied)
                .contactType(ContactType.builder().code(CONTACT_TYPE_CODE).description(CONTACT_TYPE_DESC).build())
                .contactOutcomeType(ContactOutcomeType.builder().code("COT-CODE").description(OUTCOME).build())
                .enforcementContact(true)
                .build();
    }
}

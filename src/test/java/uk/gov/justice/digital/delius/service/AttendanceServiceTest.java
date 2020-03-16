package uk.gov.justice.digital.delius.service;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.service.AttendanceService.forEntityBoolean;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.Attendance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactOutcomeType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;
import uk.gov.justice.digital.delius.jpa.standard.repository.ContactRepository;

@RunWith(MockitoJUnitRunner.class)
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
    public void getAttendancesForEventNoneAvailable() {

        when(contactRepository.findByOffenderAndEventIdEnforcement(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        assertTrue(attendanceService.getContactsForEvent(SOME_OFFENDER_ID, SOME_EVENT_ID, LocalDate.now()).isEmpty());

        verify(contactRepository).findByOffenderAndEventIdEnforcement(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    public void getAttendancesForEventMultipleAvailable() {

        final Contact contact = getContactEntity(SOME_CONTACT_ID, LocalDate.now(), "1", "1");
        when(contactRepository.findByOffenderAndEventIdEnforcement(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class))).thenReturn(singletonList(contact));

        // Act
        final List<Contact> contacts = attendanceService.getContactsForEvent(SOME_OFFENDER_ID, SOME_EVENT_ID, LocalDate.now());

        // Assert
        assertThat(contacts).hasSize(1);
        assertThat(contacts.get(0)).isSameAs(contact);
        verify(contactRepository).findByOffenderAndEventIdEnforcement(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class));
        verifyNoMoreInteractions(contactRepository);
    }

    @Test
    public void forEntityBooleanNull() {
        assertThat(forEntityBoolean(null)).isFalse();
    }

    @Test
    public void forEntityBooleanIs1() {
        assertThat(forEntityBoolean("1")).isTrue();
    }

    @Test
    public void forEntityBooleanIs0() {
        assertThat(forEntityBoolean("0")).isFalse();
    }

    @Test
    public void attendancesFor() {
        final LocalDate attendanceDate = LocalDate.of(2000, Month.APRIL, 20);
        final Contact contact = getContactEntity(SOME_CONTACT_ID, attendanceDate, "1", null);

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
                .enforcement("1")
                .build();
    }
}

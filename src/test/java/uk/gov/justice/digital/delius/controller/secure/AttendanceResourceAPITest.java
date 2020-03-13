package uk.gov.justice.digital.delius.controller.secure;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.Attendance;
import uk.gov.justice.digital.delius.data.api.Attendances;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.service.AttendanceService;
import uk.gov.justice.digital.delius.service.AttendanceServiceTest;
import uk.gov.justice.digital.delius.service.OffenderService;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(MockitoJUnitRunner.class)
public class AttendanceResourceAPITest {

    private static final Long SOME_EVENT_ID = 12342L;
    private static final Long SOME_OFFENDER_ID = 2500343964L;
    private static final String SOME_CRN = "X320741";
    private static final Long SOME_CONTACT_ID_1 = 7856L;
    private static final Long SOME_CONTACT_ID_2 = 8756L;
    private static final String PATH_FORMAT = "/secure/offenders/crn/%s/convictions/%s/attendances";
    private static final String PATH = String.format(PATH_FORMAT, SOME_CRN, SOME_EVENT_ID);

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private OffenderService offenderService;

    @Before
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new AttendanceResource(attendanceService, offenderService),
                new SecureControllerAdvice()
        );
    }

    @Test
    public void givenNoMatchingEventThenRespondWithStatusNotFound() {
        final Long nonMatchingEventId = 99L;
        final String expectedMsg = String.format(AttendanceResource.MSG_ATTENDANCES_NOT_FOUND, nonMatchingEventId);

        when(offenderService.offenderIdOfCrn(SOME_CRN)).thenReturn(Optional.of(SOME_OFFENDER_ID));
        when(attendanceService.getContactsForEvent(eq(SOME_OFFENDER_ID), eq(nonMatchingEventId), any(LocalDate.class))).thenReturn(Optional.empty());

        given()
            .when()
            .get(String.format(PATH_FORMAT, SOME_CRN, nonMatchingEventId))
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("developerMessage", containsString(expectedMsg));

        verify(offenderService).offenderIdOfCrn(SOME_CRN);
        verify(attendanceService).getContactsForEvent(eq(SOME_OFFENDER_ID), eq(nonMatchingEventId), any(LocalDate.class));
        verifyNoMoreInteractions(attendanceService, offenderService);
    }

    @Test
    public void givenNoOffenderIdForCrnThenRespondWithStatusNotFound() {
        final String expectedMsg = String.format(AttendanceResource.MSG_OFFENDER_NOT_FOUND, SOME_CRN);

        when(offenderService.offenderIdOfCrn(SOME_CRN)).thenReturn(Optional.empty());

        given()
            .when()
            .get(PATH)
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("developerMessage", containsString(expectedMsg));

        verify(offenderService).offenderIdOfCrn(SOME_CRN);
        verifyNoMoreInteractions(attendanceService, offenderService);
    }

    @Test
    public void getMultipleAttendancesReturnsOk() {

        final LocalDate attendanceDate1 = LocalDate.of(2019, Month.AUGUST, 24);
        final LocalDate attendanceDate2 = LocalDate.of(2020, Month.FEBRUARY, 29);
        final Contact contact1 = AttendanceServiceTest.getContactEntity(SOME_CONTACT_ID_1, attendanceDate1, null, null);
        final Contact contact2 = AttendanceServiceTest.getContactEntity(SOME_CONTACT_ID_2, attendanceDate2, "1", "1");
        final List<Contact> contacts = Arrays.asList(contact1, contact2);

        when(offenderService.offenderIdOfCrn(SOME_CRN)).thenReturn(Optional.of(SOME_OFFENDER_ID));
        when(attendanceService.getContactsForEvent(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class)))
            .thenReturn(Optional.of(contacts));

        // Act
        final Attendances actual = given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(PATH)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(Attendances.class);

        // assert
        assertThat(actual.getAttendances().stream()).isNotEmpty().hasSize(2);
        assertThat(actual.getAttendances().stream()).doesNotContainNull().doesNotHaveDuplicates();
        final Attendance attendance1 = actual.getAttendances().stream().filter(att -> SOME_CONTACT_ID_1.equals(att.getContactId())).findFirst().get();
        assertFalse(attendance1.isAttended());
        assertFalse(attendance1.isComplied());
        final Attendance attendance2 = actual.getAttendances().stream().filter(att -> SOME_CONTACT_ID_2.equals(att.getContactId())).findFirst().get();
        assertTrue(attendance2.isAttended());
        assertTrue(attendance2.isComplied());

        verify(offenderService).offenderIdOfCrn(SOME_CRN);
        verify(attendanceService).getContactsForEvent(eq(SOME_OFFENDER_ID), eq(SOME_EVENT_ID), any(LocalDate.class));
        verifyNoMoreInteractions(attendanceService, offenderService);
    }

    @Test
    public void getBadRequestWithIllegalEventId() {

        // Act
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(PATH_FORMAT, SOME_CRN, "XXXXX"))
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

}
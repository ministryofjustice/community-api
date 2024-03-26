package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.Attendances;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;
import uk.gov.justice.digital.delius.service.AttendanceService;
import uk.gov.justice.digital.delius.service.AttendanceServiceTest;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@ExtendWith(MockitoExtension.class)
public class AttendanceResourceTest {

    private static final Long SOME_EVENT_ID = 12342L;
    private static final Long SOME_OFFENDER_ID = 2500343964L;
    private static final String SOME_CRN = "X320741";
    private static final Long SOME_CONTACT_ID_1 = 7856L;
    private static final String FILTER_PATH_FORMAT = "/secure/offenders/crn/%s/convictions/%s/attendancesFilter";
    private static final String FILTER_PATH = String.format(FILTER_PATH_FORMAT, SOME_CRN, SOME_EVENT_ID);

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private OffenderService offenderService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new AttendanceResource(attendanceService, offenderService),
                new SecureControllerAdvice()
        );
    }

    @Test
    public void whenGetAttendances_thenReturnOk() {

        final Contact contact = AttendanceServiceTest.getContactEntity(SOME_CONTACT_ID_1, LocalDate.of(2020, Month.FEBRUARY, 29), null, null);
        final LocalDate today = LocalDate.now();

        when(offenderService.offenderIdOfCrn(SOME_CRN)).thenReturn(Optional.of(SOME_OFFENDER_ID));
        when(attendanceService.getContactsForEvent(SOME_OFFENDER_ID, SOME_EVENT_ID, LocalDate.now()))
            .thenReturn(List.of(contact));

        // Act
        final Attendances actual = given()
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(FILTER_PATH)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(Attendances.class);

        // assert
        assertThat(actual.getAttendances().stream()).isNotEmpty().hasSize(1);
        verify(offenderService).offenderIdOfCrn(SOME_CRN);
        verify(attendanceService).getContactsForEvent(SOME_OFFENDER_ID, SOME_EVENT_ID, today);
        verifyNoMoreInteractions(attendanceService, offenderService);
    }
}

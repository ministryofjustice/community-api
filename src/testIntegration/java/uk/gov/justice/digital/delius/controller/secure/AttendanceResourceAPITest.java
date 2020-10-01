package uk.gov.justice.digital.delius.controller.secure;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.data.api.Attendance;
import uk.gov.justice.digital.delius.data.api.Attendance.ContactTypeDetail;
import uk.gov.justice.digital.delius.data.api.Attendances;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AttendanceResourceAPITest extends IntegrationTestBase {

    private static final Long KNOWN_EVENT_ID = 2500295343L;
    private static final String KNOWN_CRN = "X320741";
    private static final String PATH_FORMAT = "/offenders/crn/%s/convictions/%s/attendances";
    private static final String FILTER_PATH_FORMAT = "/offenders/crn/%s/convictions/%s/attendancesFilter";
    private static final String PATH = String.format(PATH_FORMAT, KNOWN_CRN, KNOWN_EVENT_ID);
    private static final String FILTER_PATH = String.format(FILTER_PATH_FORMAT, KNOWN_CRN, KNOWN_EVENT_ID);

    @Test
    public void normalGetAttendances() {
        final Attendances attendances = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(Attendances.class);

        assertThat(attendances.getAttendances().stream()).hasSize(3);
    }

    @Test
    public void getBadRequest400() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PATH_FORMAT, "XXX", "XXX"))
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void getKnownCrnButEventIdNotFound200() {
        final String eventId = "923213723";
        final Attendances attendances = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(PATH_FORMAT, KNOWN_CRN, eventId))
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(Attendances.class);

        assertTrue(attendances.getAttendances().isEmpty());
    }

    @Test
    public void whenGetAttendances_ThenReturnSingleMatch() {
        final Attendances attendances = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(FILTER_PATH)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(Attendances.class);

        assertThat(attendances.getAttendances().stream()).hasSize(1);
        assertThat(attendances.getAttendances()).containsExactlyInAnyOrder(Attendance.builder()
            .attended(true)
            .attendanceDate(LocalDate.of(2020, Month.SEPTEMBER, 4))
            .complied(false)
            .contactType(ContactTypeDetail.builder().code("C084").description("3 Way Meeting (NS)").build())
            .outcome("Appointment Kept")
            .contactId(2502719240L)
            .build());
    }

    @Test
    public void givenCrnWithNullEnforcementAndOutcome_whenGetAttendances_ThenReturnNone() {

        String path = String.format(FILTER_PATH_FORMAT, "X320811", "2600295124");

        final Attendances attendances = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(Attendances.class);

        assertThat(attendances.getAttendances().stream()).isEmpty();
    }

}

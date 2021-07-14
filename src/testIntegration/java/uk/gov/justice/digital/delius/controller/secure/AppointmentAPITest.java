package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class AppointmentAPITest extends IntegrationTestBase {

    @Test
    public void gettingOffenderAppointmentsByCrn() {
        final var response = given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/appointments")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", greaterThan(0))
            .root("find { it.appointmentId == 2502719240 }");

        shouldReturnWellKnownAppointment(response);
    }

    @Test
    public void attemptingToGetOffenderAppointmentsByCrnButForbidden() {
        given()
            .auth().oauth2(createJwt("SOME_OTHER_ROLE"))
            .when()
            .get("/offenders/crn/X320741/appointments")
            .then()
            .assertThat()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void attemptingToGetOffenderAppointmentsByCrnButMissing() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/A999999/appointments")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void gettingOffenderAppointmentByCrn() {
        final var response = given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/appointments/2502719240")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value());

        shouldReturnWellKnownAppointment(response);
    }

    @Test
    public void attemptingToGetOffenderAppointmentByCrnButForbidden() {
        given()
            .auth().oauth2(createJwt("SOME_OTHER_ROLE"))
            .when()
            .get("/offenders/crn/X320741/appointments/2502719240")
            .then()
            .assertThat()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void attemptingToGetOffenderAppointmentByCrnButMissing() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/appointments/100")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void attemptingToGetOffenderAppointmentByCrnButMissingOffender() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/A999999/appointments/2502719240")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void attemptingToGetNonAppointmentContact() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320811/appointments/2502719242")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void attemptingToGetSoftDeletedAppointment() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320811/appointments/2502719243")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private void shouldReturnWellKnownAppointment(ValidatableResponse response) {
        response
            .body("appointmentId", equalTo(2502719240L))
            .body("appointmentStart", equalTo("2020-09-04T00:00:00+01:00"))
            .body("appointmentEnd", equalTo("2020-09-04T00:00:00+01:00"))
            .body("type.contactType", equalTo("C084"))
            .body("type.description", equalTo("3 Way Meeting (NS)"))
            .body("type.requiresLocation", equalTo("REQUIRED"))
            .body("type.orderTypes", equalTo(List.of("CJA", "LEGACY")))
            .body("officeLocation", equalTo(null))
            .body("notes", equalTo("The notes field"))
            .body("provider.code", equalTo("N02"))
            .body("provider.description", equalTo("NPS North East"))
            .body("team.code", equalTo("N02SP5"))
            .body("team.description", equalTo("BRADFORD\\BULLS SP"))
            .body("staff.code", equalTo("N02SP5U"))
            .body("staff.forenames", equalTo("Unallocated"))
            .body("staff.surname", equalTo("Staff"))
            .body("staff.unallocated", equalTo(true))
            .body("sensitive", equalTo(null))
            .body("outcome.code", equalTo("APPK"))
            .body("outcome.description", equalTo("Appointment Kept"))
            .body("outcome.attended", equalTo(true))
            .body("outcome.complied", equalTo(null))
            .body("outcome.hoursCredited", equalTo(null));
    }
}

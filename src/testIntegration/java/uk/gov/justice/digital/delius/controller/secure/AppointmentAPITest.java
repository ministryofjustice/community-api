package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.data.api.AppointmentDetail;
import uk.gov.justice.digital.delius.data.api.AppointmentType.OrderType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class AppointmentAPITest extends IntegrationTestBase {

    @Test
    public void gettingOffenderAppointmentsByCrn() {

        final var appointments = given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/appointments")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", greaterThan(0))
            .root("find { it.appointmentId == 2502719240 }")
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
            .body("outcome.hoursCredited", equalTo(null))
            .extract()
            .body()
            .as(AppointmentDetail[].class);

        assertThat(appointments).hasSize(1);
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
}

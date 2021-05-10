package uk.gov.justice.digital.delius.controller.secure;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class AppointmentMetaAPITest extends IntegrationTestBase {

    @Test
    public void attemptingToGetAllAppointmentTypesWithoutCorrectRole() {
        final var token = createJwt("SOME_OTHER_ROLE");
        given()
            .auth().oauth2(token)
            .when()
            .get("/appointment-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void gettingAllAppointmentTypes() {
        final var token = createJwt("ROLE_COMMUNITY");

        final var alcohol = withArgs("CITA");
        final var homeVisit = withArgs("CHVS");
        final var other = withArgs("C031");
        final var polygraph = withArgs("POLY01");

        given()
            .auth().oauth2(token)
            .when()
            .get("/appointment-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", greaterThan(0))
            .root("find { it.contactType == '%s' }")
            .body("description", alcohol, equalTo("Citizenship Alcohol Session (NS)"))
            .body("requiresLocation", alcohol, equalTo("REQUIRED"))
            .body("orderTypes", alcohol, equalTo(List.of("CJA")))
            .body("description", homeVisit, equalTo("Home Visit to Case (NS)"))
            .body("requiresLocation", homeVisit, equalTo("NOT_REQUIRED"))
            .body("orderTypes", homeVisit, equalTo(List.of("CJA", "LEGACY")))
            .body("description", other, equalTo("Other Appointment (Non NS)"))
            .body("requiresLocation", other, equalTo("OPTIONAL"))
            .body("orderTypes", other, equalTo(List.of("CJA", "LEGACY")))
            .body("orderTypes", polygraph, equalTo(List.of()));
    }
}

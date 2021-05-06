package uk.gov.justice.digital.delius.controller.secure;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class AppointmentMetaAPITest extends IntegrationTestBase {

    @Test
    public void gettingAllAppointmentTypes() {
        final var token = createJwt("ROLE_COMMUNITY");

        final var session = withArgs("APAT");
        final var homeVisit = withArgs("CHVS");
        final var other = withArgs("C031");

        given()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .when()
            .get("/appointment-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", greaterThan(0))
            .root("find { it.contactType == '%s' }")
            .body("description", session, equalTo("Programme Session (NS)"))
            .body("requiresLocation", session, equalTo("REQUIRED"))
            .body("description", homeVisit, equalTo("Home Visit to Case (NS)"))
            .body("requiresLocation", homeVisit, equalTo("NOT_REQUIRED"))
            .body("description", other, equalTo("Other Appointment (Non NS)"))
            .body("requiresLocation", other, equalTo("OPTIONAL"));
    }
}

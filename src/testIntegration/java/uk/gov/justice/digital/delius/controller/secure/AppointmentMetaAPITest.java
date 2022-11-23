package uk.gov.justice.digital.delius.controller.secure;

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
            .body("wholeOrderLevel", alcohol, equalTo(false))
            .body("offenderLevel", alcohol, equalTo(false))
            .body("requirementTypeMainCategories.size", alcohol, equalTo(3))
            .body("requirementTypeMainCategories[0].code", alcohol, equalTo("Q"))
            .body("requirementTypeMainCategories[0].description", alcohol, equalTo("Specified Activity"))
            .body("requirementTypeMainCategories[1].code", alcohol, equalTo("Y"))
            .body("requirementTypeMainCategories[1].description", alcohol, equalTo("Supervision"))
            .body("requirementTypeMainCategories[2].code", alcohol, equalTo("F"))
            .body("requirementTypeMainCategories[2].description", alcohol, equalTo("Rehabilitation Activity Requirement (RAR)"))

            .body("description", homeVisit, equalTo("Home Visit to Case (NS)"))
            .body("requiresLocation", homeVisit, equalTo("NOT_REQUIRED"))
            .body("orderTypes", homeVisit, equalTo(List.of("CJA", "LEGACY")))
            .body("wholeOrderLevel", homeVisit, equalTo(true))
            .body("offenderLevel", homeVisit, equalTo(false))
            .body("requirementTypeMainCategories.size", homeVisit, equalTo(2))
            .body("requirementTypeMainCategories[0].code", homeVisit, equalTo("Y"))
            .body("requirementTypeMainCategories[0].description", homeVisit, equalTo("Supervision"))
            .body("requirementTypeMainCategories[1].code", homeVisit, equalTo("RM39"))
            .body("requirementTypeMainCategories[1].description", homeVisit, equalTo("Local - Herts Local Activities"))

            .body("description", other, equalTo("Other Appointment (Non NS)"))
            .body("requiresLocation", other, equalTo("OPTIONAL"))
            .body("orderTypes", other, equalTo(List.of("CJA", "LEGACY")))

            .body("orderTypes", polygraph, equalTo(List.of()));
    }
}

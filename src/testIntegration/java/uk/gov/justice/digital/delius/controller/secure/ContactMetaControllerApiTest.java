package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;

public class ContactMetaControllerApiTest extends IntegrationTestBase {

    @Test
    public void attemptingToGetContactTypesWithoutCorrectRole() {
        final var token = createJwt("SOME_OTHER_ROLE");
        given()
            .auth().oauth2(token)
            .when()
            .get("/contact-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void getAllContactTypes() {
        final var token = createJwt("ROLE_COMMUNITY");

        final var officeVisit = withArgs("COAP");

        given()
            .auth().oauth2(token)
            .when()
            .get("/contact-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(929))
            .root("find { it.code == '%s' }")
            .body("description", officeVisit, equalTo("Planned Office Visit (NS)"))
            .body("appointment", officeVisit, equalTo(true));
    }

    @Test
    public void getContactTypesForACategory() {
        final var token = createJwt("ROLE_COMMUNITY");

        final var phoneContact = withArgs("CTOB");
        final var emailContact = withArgs("CMOB");

        given()
            .auth().oauth2(token)
            .when()
            .queryParam("categories", "LT")
            .get("/contact-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(43))
            .root("find { it.code == '%s' }")
            .body("description", phoneContact, equalTo("Phone Contact to Offender"))
            .body("appointment", phoneContact, equalTo(false))
            .body("description", emailContact, equalTo("eMail/Text to Offender"))
            .body("appointment", emailContact, equalTo(false));
    }

    @Test
    public void getContactOutcomes() {
        final var token = createJwt("ROLE_COMMUNITY");

        final var attendedFailedToComply = withArgs("AFTC");

        final var firstWarningLetter = withArgs("AFTC", "EA02");

        given()
            .auth().oauth2(token)
            .when()
            .get("/contact-types/COAP/outcome-types")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("outcomeRequired", equalTo("REQUIRED"))
            .body("outcomeTypes.size()", equalTo(23))
            .root("outcomeTypes.find { it.code == '%s' }")
            .body("description", attendedFailedToComply, equalTo("Attended - Failed to Comply"))
            .body("compliantAcceptable", attendedFailedToComply, equalTo(false))
            .body("attendance", attendedFailedToComply, equalTo(true))
            .body("actionRequired",attendedFailedToComply, equalTo(true))
            .body("enforceable", attendedFailedToComply, equalTo(true))
            .body("enforcements.size()", attendedFailedToComply, equalTo(21))
            .root("outcomeTypes.find { it.code == '%s' }.enforcements.find { it.code == '%s' }")
            .body("description", firstWarningLetter, equalTo("First Warning Letter Sent"))
            .body("outstandingContactAction", firstWarningLetter, equalTo(false))
            .body("responseByPeriod", firstWarningLetter, equalTo(7));
    }

}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_PersonalContactsTest extends IntegrationTestBase {
    @Test
    void requiresRoleCommunity() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_BANANAS"))
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/personalContacts")
            .then()
            .statusCode(403);
    }

    @Test
    void respondsWithNotFoundWhenOffenderMissing() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/SOME_MISSING_OFFENDER/personalContacts")
            .then()
            .statusCode(404);
    }

    @Test
    void returnsEmptyListWhenOffenderHasNoPersonalContacts() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320811/personalContacts")
            .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("[]"));
    }

    @Test
    void getsPersonalContacts() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/personalContacts")
            .then()
            .assertThat()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .root("find { it.personalContactId == 2500058493 }")
            .body("title", equalTo("Lady"))
            .body("firstName", equalTo("Smile"))
            .body("surname", equalTo("Barry"))
            .body("relationship", equalTo("Good friend"));
    }
}

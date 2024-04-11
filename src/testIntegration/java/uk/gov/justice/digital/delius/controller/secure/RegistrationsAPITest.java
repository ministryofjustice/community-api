package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RegistrationsAPITest extends IntegrationTestBase {
    private static final String CRN = "X320741";

    @Test
    public void canGetRegistrationByCRN() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/registrations", CRN)
                .then()
                .statusCode(200)
                .body("registrations[2].register.description", is("Public Protection"))
                .body("registrations[2].deregisteringNotes", nullValue())
                .body("registrations[3].deregisteringNotes", is("Ok again now"));
    }

    @Test
    public void canGetActiveRegistrationByCRN() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/{crn}/registrations?activeOnly=true", CRN)
            .then()
            .statusCode(200).body("registrations", hasSize(2));
    }

    @Test
    public void cantGetActiveRegistrationByCRN_ifExcluded() {
        given()
            .auth().oauth2(createJwtWithUsername("bob.jones", "ROLE_COMMUNITY"))
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X440877/registrations")
            .then()
            .statusCode(403)
            .body("developerMessage", equalTo("You are excluded from viewing this offender record. Please contact a system administrator"));
    }
}

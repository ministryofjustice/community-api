package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RegistrationsAPITest extends IntegrationTestBase {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String OFFENDER_ID = "2500343964";
    private static final String CRN = "X320741";

    @Test
    public void mustHaveCommunityRole() {
        final var token = createJwt("ROLE_BANANAS");

        given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/offenderId/{offenderId}/registrations", OFFENDER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    public void canGetRegistrationByOffenderId() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/offenderId/{offenderId}/registrations", OFFENDER_ID)
                .then()
                .statusCode(200)
                .body("registrations[0].register.description", is("Public Protection"))
                .body("registrations[0].startDate", is("2019-10-11"))
                .body("registrations[0].deregisteringNotes", nullValue())
                .body("registrations[1].deregisteringNotes", is("Ok again now"));

    }

    @Test
    public void canGetRegistrationByNOMSNumber() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/{nomsNumber}/registrations", NOMS_NUMBER)
                .then()
                .statusCode(200)
                .body("registrations[0].register.description", is("Public Protection"))
                .body("registrations[0].deregisteringNotes", nullValue())
                .body("registrations[1].deregisteringNotes", is("Ok again now"))
                .body("registrations[1].numberOfPreviousDeregistrations", is(2));
    }

    @Test
    public void canGetRegistrationByCRN() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/registrations", CRN)
                .then()
                .statusCode(200)
                .body("registrations[0].register.description", is("Public Protection"))
                .body("registrations[0].deregisteringNotes", nullValue())
                .body("registrations[1].deregisteringNotes", is("Ok again now"));
    }
}

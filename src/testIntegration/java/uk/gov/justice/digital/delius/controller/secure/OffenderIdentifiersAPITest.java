package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffenderIdentifiersAPITest extends IntegrationTestBase {
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
                .get("/offenders/offenderId/{offenderId}/identifiers", OFFENDER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    public void canGetOffenderIdentifiersByOffenderId() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/offenderId/{offenderId}/identifiers", OFFENDER_ID)
                .then()
                .statusCode(200)
                .body("offenderId", is(Long.valueOf(OFFENDER_ID)))
                .body("primaryIdentifiers.crn", is(CRN))
                .body("primaryIdentifiers.nomsNumber", is(NOMS_NUMBER))
                .body("additionalIdentifiers[0].value", is("A1234CR"))
                .body("additionalIdentifiers[1].value", is("X123456"));

    }

    @Test
    public void canGetOffenderIdentifiersByCRN() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/identifiers", CRN)
                .then()
                .statusCode(200)
                .body("primaryIdentifiers.nomsNumber", is(NOMS_NUMBER))
                .body("additionalIdentifiers[0].value", is("A1234CR"))
                .body("additionalIdentifiers[1].value", is("X123456"));
    }

}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class OffenderIdentifiersAPITest extends IntegrationTestBase {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String CRN = "X320741";

    @Test
    void canGetOffenderIdentifiersByCRN() {
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

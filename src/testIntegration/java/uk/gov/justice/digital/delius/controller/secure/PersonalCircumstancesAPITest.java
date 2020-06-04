package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class PersonalCircumstancesAPITest extends IntegrationTestBase {
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
                .get("/offenders/offenderId/{offenderId}/personalCircumstances", OFFENDER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    public void canGetPersonalCircumstancesByOffenderId() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/offenderId/{offenderId}/personalCircumstances", OFFENDER_ID)
                .then()
                .statusCode(200)
                .body("personalCircumstances[0].personalCircumstanceType.description", is("AP - Medication in Posession  - Assessment"))
                .body("personalCircumstances[0].startDate", is("2019-09-11"));

    }

    @Test
    public void canGetPersonalCircumstancesByNOMSNumber() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/{nomsNumber}/personalCircumstances", NOMS_NUMBER)
                .then()
                .statusCode(200)
                .body("personalCircumstances[0].personalCircumstanceType.description", is("AP - Medication in Posession  - Assessment"));
    }

    @Test
    public void canGetPersonalCircumstancesByCRN() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/personalCircumstances", CRN)
                .then()
                .statusCode(200)
                .body("personalCircumstances[0].personalCircumstanceType.description", is("AP - Medication in Posession  - Assessment"));
    }

}

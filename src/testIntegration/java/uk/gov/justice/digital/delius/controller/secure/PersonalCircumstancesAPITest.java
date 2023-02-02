package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class PersonalCircumstancesAPITest extends IntegrationTestBase {
    private static final String CRN = "X320741";

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

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getPssRequirements extends IntegrationTestBase {
    private static final String PSS_REQUIREMENTS_PATH = "/offenders/crn/%s/convictions/%s/sentences/%s/pssRequirements";

    private static final String KNOWN_OFFENDER_CRN = "X320741";
    private static final Long KNOWN_CONVICTION_ID = 1234L;
    private static final Long KNOWN_SENTENCE_ID = 12345L;

    @Test
    public void getPssRequirementsBySentenceId() {

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PSS_REQUIREMENTS_PATH, KNOWN_OFFENDER_CRN, KNOWN_CONVICTION_ID, KNOWN_SENTENCE_ID))
                .then()
                .statusCode(200)
                .body("pssRequirements[0].pssRequirementId", equalTo("250015755"))
                .body("[2].pssRequirements[0].type.description", equalTo( "Standard 7 Conditions"))
                .body("[2].pssRequirements[0].subType.description", equalTo( "?"))
                .body("[2].pssRequirements[0].active", equalTo(false))
                ;
    }

    @Test
    public void getPssRequirementsBySentenceId_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PSS_REQUIREMENTS_PATH, "X777777", KNOWN_CONVICTION_ID, KNOWN_SENTENCE_ID))
                .then()
                .statusCode(404);
    }

    @Test
    public void getPssRequirementsBySentenceId_convictionNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PSS_REQUIREMENTS_PATH, KNOWN_OFFENDER_CRN, 9999999L, KNOWN_SENTENCE_ID))
                .then()
                .statusCode(404);
    }

    @Test
    public void getPssRequirementsBySentenceId_sentenceNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PSS_REQUIREMENTS_PATH, KNOWN_OFFENDER_CRN, KNOWN_CONVICTION_ID, 9999L))
                .then()
                .statusCode(404);
    }
}

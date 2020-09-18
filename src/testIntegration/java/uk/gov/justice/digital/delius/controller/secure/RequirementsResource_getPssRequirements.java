package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RequirementsResource_getPssRequirements extends IntegrationTestBase {
    private static final String PSS_REQUIREMENTS_PATH = "/offenders/crn/%s/convictions/%s/pssRequirements";

    private static final String KNOWN_OFFENDER_CRN = "X320741";
    private static final Long KNOWN_CONVICTION_ID = 2500295345L;

    @Test
    public void getPssRequirementsByConvictionId() {

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PSS_REQUIREMENTS_PATH, KNOWN_OFFENDER_CRN, KNOWN_CONVICTION_ID))
                .then()
                .statusCode(200)
                .body("pssRequirements[0].type.description", equalTo( "Standard 7 Conditions"))
                .body("pssRequirements[0].subType.description", equalTo( "Adult Custody 12m plus"))
                .body("pssRequirements[0].active", equalTo(false))
                ;
    }

    @Test
    public void getPssRequirementsByConvictionId_convictionNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PSS_REQUIREMENTS_PATH, KNOWN_OFFENDER_CRN, 9999999L))
                .then()
                .statusCode(404);
    }
}

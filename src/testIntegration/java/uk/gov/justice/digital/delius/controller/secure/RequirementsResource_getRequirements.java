package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RequirementsResource_getRequirements extends IntegrationTestBase {
    private static final String REQUIREMENTS_PATH = "/offenders/crn/%s/convictions/%s/requirements";

    private static final String KNOWN_OFFENDER_CRN = "X320741";
    private static final Long KNOWN_CONVICTION_ID = 2500295343L;

    @Test
    public void getLicenceConditionsByConvictionId() {

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(format(REQUIREMENTS_PATH, KNOWN_OFFENDER_CRN, KNOWN_CONVICTION_ID))
                .then()
                .statusCode(200)
                .body("requirements[0].restrictive", equalTo( false));
    }

    @Test
    public void canRetrieveAllRequirements() {

        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(format(REQUIREMENTS_PATH, KNOWN_OFFENDER_CRN, KNOWN_CONVICTION_ID))
            .then()
            .statusCode(200)
            .body("requirements", hasSize(2));

    }
    @Test
    public void canRetrieveActiveRequirements() {

        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(format(REQUIREMENTS_PATH, "X320811", "2600295124") + "?activeOnly=true")
            .then()
            .statusCode(200)
            .body("requirements", hasSize(2));

    }
    
}

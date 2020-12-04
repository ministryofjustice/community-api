package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RequirementsResource_getLicenceConditions extends IntegrationTestBase {
    private static final String LICENCE_CONDITIONS_PATH = "/offenders/crn/%s/convictions/%s/licenceConditions";

    private static final String KNOWN_OFFENDER_CRN = "X320741";
    private static final Long KNOWN_CONVICTION_ID = 2500295345L;

    @Test
    public void getLicenceConditionsByConvictionId() {

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(LICENCE_CONDITIONS_PATH, KNOWN_OFFENDER_CRN, KNOWN_CONVICTION_ID))
                .then()
                .statusCode(200)
                .body("licenceConditions[0].licenceConditionTypeMainCat.description", equalTo( "Local - Enforcement Activity"))
                .body("licenceConditions[0].licenceConditionTypeMainCat.code", equalTo( "LC19"))
                .body("licenceConditions[0].licenceConditionTypeSubCat.description", equalTo("Licence"))
                .body("licenceConditions[0].licenceConditionTypeSubCat.code", equalTo("L"))
                .body("licenceConditions[0].active", equalTo(true))
                ;
    }

    @Test
    public void getLicenceConditionsByConvictionId_convictionNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(LICENCE_CONDITIONS_PATH, KNOWN_OFFENDER_CRN, 9999999L))
                .then()
                .statusCode(404);
    }
}

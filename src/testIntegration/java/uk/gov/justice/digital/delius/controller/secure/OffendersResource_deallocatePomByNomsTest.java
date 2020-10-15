package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;

import static io.restassured.RestAssured.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(FlywayRestoreExtension.class)
public class OffendersResource_deallocatePomByNomsTest extends IntegrationTestBase {

    @Test
    public void badAuth_returnsUnauthorized() {
        given()
                .auth()
                .oauth2("BAD_TOKEN")
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(401);
    }

    @Test
    public void missingToken_returnsForbidden() {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(403);
    }

    @Test
    public void noRole_returnsForbidden() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(403);
    }

    @Test
    public void missingNomsNumber_returnsBadRequest() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber//prisonOffenderManager")
                .then()
                .statusCode(400);
    }

    @Test
    public void offenderHasNoPom_returnsConflict() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(409);
    }

    @Test
    public void offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .delete("/offenders/nomsNumber/DOES_NOT_EXIST/prisonOffenderManager")
                .then()
                .statusCode(404);
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(FlywayRestoreExtension.class)
public class OffendersResource_deallocatePomByNomsTest extends IntegrationTestBase {

    @Test
    public void badAuth_returnsUnauthorized() {
        given()
                .auth()
                .oauth2("BAD_TOKEN")
                .when()
                .delete("/offenders/nomsNumber/G0560UO/prisonOffenderManager")
                .then()
                .statusCode(401);
    }

    @Test
    public void missingToken_returnsForbidden() {
        given()
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
                .when()
                .delete("/offenders/nomsNumber/G9643VP/prisonOffenderManager")
                .then()
                .statusCode(409);
    }

    @Test
    public void offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .when()
                .delete("/offenders/nomsNumber/DOES_NOT_EXIST/prisonOffenderManager")
                .then()
                .statusCode(404);
    }

    private void allocateUserPom(final String nomsNumber) {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunityAndCustodyUpdate())
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerOf(2500057541L, "BWI"))
                .when()
                .put(format("/offenders/nomsNumber/%s/prisonOffenderManager", nomsNumber))
                .then()
                .statusCode(200);

        final CommunityOrPrisonOffenderManager pomBeforeDeallocate = getPom(nomsNumber);
        assertThat(pomBeforeDeallocate.getStaff().getForenames()).isEqualTo("User");
        assertThat(pomBeforeDeallocate.getStaff().getSurname()).isEqualTo("POM");
    }

    private CommunityOrPrisonOffenderManager getPom(final String nomsNumber) {
        final var offenderManagers = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(format("/offenders/nomsNumber/%s/allOffenderManagers", nomsNumber))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        return Arrays.stream(offenderManagers)
                .filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)
                .findFirst()
                .orElseThrow();
    }

    private String createPrisonOffenderManagerOf(final Long staffId, final String nomsPrisonInstitutionCode) {
        return writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .staffId(staffId)
                .nomsPrisonInstitutionCode(nomsPrisonInstitutionCode)
                .build());
    }

}

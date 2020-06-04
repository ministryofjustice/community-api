package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_GetAllOffenderManagersAPITest extends IntegrationTestBase {

    @Test
    public void canGetAllOffenderManagersByNOMSNumber() {
        final var offenderManagers = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/allOffenderManagers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CommunityOrPrisonOffenderManager[].class);

        assertThat(offenderManagers).hasSize(2);

        final var communityOffenderManager = Stream.of(offenderManagers).filter(not(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager)).findAny().orElseThrow();
        final var prisonOffenderManager = Stream.of(offenderManagers).filter(CommunityOrPrisonOffenderManager::getIsPrisonOffenderManager).findAny().orElseThrow();

        assertThat(communityOffenderManager.getIsResponsibleOfficer()).isFalse();
        assertThat(communityOffenderManager.getIsUnallocated()).isTrue();
        assertThat(communityOffenderManager.getProbationArea()).isNotNull();
        assertThat(communityOffenderManager.getProbationArea().getInstitution()).isNull();
        assertThat(communityOffenderManager.getStaff()).isNotNull();
        assertThat(communityOffenderManager.getTeam()).isNotNull();
        assertThat(communityOffenderManager.getStaffCode()).isEqualTo("N02AAMU");

        assertThat(prisonOffenderManager.getIsResponsibleOfficer()).isTrue();
        assertThat(prisonOffenderManager.getIsUnallocated()).isFalse();
        assertThat(prisonOffenderManager.getProbationArea()).isNotNull();
        assertThat(prisonOffenderManager.getProbationArea().getInstitution()).isNotNull();
        assertThat(prisonOffenderManager.getStaff()).isNotNull();
        assertThat(prisonOffenderManager.getTeam()).isNotNull();
        assertThat(prisonOffenderManager.getStaffCode()).isEqualTo("BWIA010");
    }

    @Test
    public void getAllOffenderManagersByNOMSNumberReturn404WhenOffenderDoesNotExist() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/DOESNOTEXIST/allOffenderManagers")
                .then()
                .statusCode(404);
    }

}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderManager;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getOffenderByCrn extends IntegrationTestBase {
    @Test
    public void canGetOffenderDetailsByCrn() {
        final var offenderDetail = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/all")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderDetail.class);

      assertThat(offenderDetail.getOtherIds().getCrn()).isEqualTo("X320741");
      final var offenderManager = offenderDetail.getOffenderManagers().stream().filter(OffenderManager::getActive).findAny();
      assertThat(offenderManager).isPresent();
      assertThat(offenderManager.orElseThrow().getTeam().getCode()).isEqualTo("N02AAM");
      assertThat(offenderManager.orElseThrow().getStaff().getCode()).isEqualTo("N02AAMU");
      assertThat(offenderManager.orElseThrow().getStaff().getForenames()).isEqualTo("Unallocated");
      assertThat(offenderManager.orElseThrow().getStaff().isUnallocated()).isTrue();
      assertThat(offenderManager.orElseThrow().getTeam().getLocalDeliveryUnit().getCode()).isEqualTo("N02OMIC");
      assertThat(offenderManager.orElseThrow().getTeam().getLocalDeliveryUnit().getDescription()).isEqualTo("OMiC POM Responsibility");
      assertThat(offenderDetail.getCurrentTier()).isEqualTo("D2");
    }

  @Test
  public void canGetOffenderSummaryByCrn() {
    final var offenderDetail = given()
      .auth()
      .oauth2(tokenWithRoleCommunity())
      .contentType(APPLICATION_JSON_VALUE)
      .when()
      .get("/offenders/crn/X320741")
      .then()
      .statusCode(200)
      .extract()
      .body()
      .as(OffenderDetailSummary.class);

    assertThat(offenderDetail.getOtherIds().getCrn()).isEqualTo("X320741");
  }

    @Test
    public void givenUserIsNotExcluded_thenAccessAllowed(){
        final var username = "bernard.beaks";
        final var path = "/offenders/crn/X440877";

        assertAccessAllowedFor(path, createJwtWithUsername(username, "ROLE_COMMUNITY"));
    }

    @Test
    public void givenUserIsExcluded_thenAccessDenied(){
        final var username = "bob.jones";
        final var path = "/offenders/crn/X440877";

        assertAccessForbiddenFor(path, createJwtWithUsername(username, "ROLE_COMMUNITY"), "You are excluded from viewing this offender record. Please contact a system administrator");

    }

    @Test
    public void givenUserIsExcluded_andScopeIgnoreExclusions_thenAccessAllowed(){
        final var username = "bob.jones";
        final var path = "/offenders/crn/X440877";

        assertAccessAllowedFor(path, createJwtWithUsername(username, "ROLE_COMMUNITY", "SCOPE_IGNORE_DELIUS_EXCLUSIONS_ALWAYS"));

    }

    @Test
    public void givenOffenderIsRestricted_andUserIsOnAllowList_thenAccessAllowed(){
        final var username = "bobby.davro";
        final var path = "/offenders/crn/X440890";

        assertAccessAllowedFor(path, createJwtWithUsername(username, "ROLE_COMMUNITY"));
    }

    @Test
    public void givenOffenderIsRestricted_andUserIsNotOnAllowList_thenAccessDenied(){
        final var username = "bob.jones";
        final var path = "/offenders/crn/X440890";

        assertAccessForbiddenFor(path, createJwtWithUsername(username, "ROLE_COMMUNITY"), "This is a restricted offender record. Please contact a system administrator");
    }

    @Test
    public void givenOffenderIsRestricted_andUserIsNotOnAllowList_andScopeIgnoreInclusions_thenAccessAllowed(){
        final var username = "bob.jones";
        final var path = "/offenders/crn/X440890";

        assertAccessAllowedFor(path, createJwtWithUsername(username, "ROLE_COMMUNITY", "SCOPE_IGNORE_DELIUS_INCLUSIONS_ALWAYS"));
    }

    @Test
    public void getOffenderSummaryByCrn_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X777777")
                .then()
                .statusCode(404);
    }

    @Test
    public void getOffenderDetailsByCrn_offenderNotFound_returnsNotFound() {
      given()
        .auth()
        .oauth2(tokenWithRoleCommunity())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get("/offenders/crn/X777777/all")
        .then()
        .statusCode(404);
    }

    @Test
    public void givenUserIsExcluded_whenAllEndpointCalled_thenAccessDenied(){
        final var username = "bob.jones";
        final var path = "/offenders/crn/X440877/all";

        assertAccessForbiddenFor(path, createJwtWithUsername(username, "ROLE_COMMUNITY"), "You are excluded from viewing this offender record. Please contact a system administrator");
    }

    @Test
    public void givenOffenderIsRestricted_andUserIsNotOnAllowList_whenAllEndpointCalled_thenAccessDenied(){
        final var username = "bob.jones";
        final var path = "/offenders/crn/X440890/all";

        assertAccessForbiddenFor(path, createJwtWithUsername(username, "ROLE_COMMUNITY"), "This is a restricted offender record. Please contact a system administrator");
    }

    private void assertAccessAllowedFor(String path, String accessToken) {
        given()
                .auth()
                .oauth2(accessToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderDetailSummary.class);

    }

    private void assertAccessForbiddenFor(String path, String accessToken, String message) {
        final var offenderDetail = given()
                .auth()
                .oauth2(accessToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(403)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(offenderDetail.getDeveloperMessage()).isEqualTo(message);
    }
}

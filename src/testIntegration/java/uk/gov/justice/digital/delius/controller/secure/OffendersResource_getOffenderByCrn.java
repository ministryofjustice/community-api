package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
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

}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderManager;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getOffenderByNomsNumber extends IntegrationTestBase {
    @Test
    public void canGetOffenderDetailsWhenSearchByNomsNumberInUpperCase() {
        final var offenderDetail = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/all")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderDetail.class);

      assertThat(offenderDetail.getOtherIds().getNomsNumber()).isEqualTo("G9542VP");
      final var offenderManager = offenderDetail.getOffenderManagers().stream().filter(OffenderManager::getActive).findAny();
      assertThat(offenderManager).isPresent();
    }
    @Test
    public void canGetOffenderDetailsWhenSearchByNomsNumberInLowerCase() {
        final var offenderDetail = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/nomsNumber/g9542vp/all")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(OffenderDetail.class);

        assertThat(offenderDetail.getOtherIds().getNomsNumber()).isEqualTo("G9542VP");
        final var offenderManager = offenderDetail.getOffenderManagers().stream().filter(OffenderManager::getActive).findAny();
        assertThat(offenderManager).isPresent();
    }

    @Test
    public void getOffenderDetailsByNomsNumber_offenderNotFound_returnsNotFound() {
      given()
        .auth()
        .oauth2(tokenWithRoleCommunity())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get("/offenders/nomsNumber/A4444FG/all")
        .then()
        .statusCode(404);
    }

}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderManager;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
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

    @Nested
    @DisplayName("When multiple records match the same noms number")
    class DuplicateNOMSNumbers{
        @Nested
        @DisplayName("When only one of the records is current")
        class OnlyOneActive{
            @Test
            @DisplayName("will return the active record")
            void willReturnTheActiveRecord() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3232DD/all")
                    .then()
                    .statusCode(200)
                    .body("otherIds.crn", equalTo("CRN35"));

                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3232DD")
                    .then()
                    .statusCode(200)
                    .body("otherIds.crn", equalTo("CRN35"));
            }
            @Test
            @DisplayName("will return a conflict response when fail on duplicate is set to true")
            void willReturnAConflictResponseWhenFailureOnDuplicate() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3232DD/all?failOnDuplicate=true")
                    .then()
                    .statusCode(409);

                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3232DD?failOnDuplicate=true")
                    .then()
                    .statusCode(409);
            }

        }
        @Nested
        @DisplayName("When both records have the same active state")
        class BothActive{
            @Test
            @DisplayName("will return a conflict response")
            void willReturnAConflictResponse() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3636DD/all")
                    .then()
                    .statusCode(409);

                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3636DD")
                    .then()
                    .statusCode(409);
            }

        }
    }

}

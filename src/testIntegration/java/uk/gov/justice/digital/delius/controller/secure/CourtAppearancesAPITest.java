package uk.gov.justice.digital.delius.controller.secure;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasic;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasicWrapper;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CourtAppearancesAPITest extends IntegrationTestBase {

    @Nested
    @DisplayName("Tests for Court Appearance by date")
    class AppearanceDate {

        @Test
        public void mustHaveCommunityRole() {
            final var token = createJwt("ROLE_BANANAS");

            given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/courtAppearances")
                .then()
                .statusCode(403);
        }

        @Test
        public void canGetCourtAppearances() {
            var result = given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .param("fromDate", "2019-09-04")
                .when()
                .get("/courtAppearances")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtAppearanceBasicWrapper.class);
            assertThat(result.getCourtAppearances()).extracting("courtAppearanceId").containsExactly(2500316926L, 2500319107L);
        }

        @Test
        public void canGetCourtAppearancesNoResults() {
            var result = given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .param("fromDate", "2034-09-05")
                .when()
                .get("/courtAppearances")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtAppearanceBasicWrapper.class);
            assertThat(result.getCourtAppearances()).hasSize(0);
        }
    }

    @Nested
    @DisplayName("Tests for Court Appearance by CRN")
    class AppearancesByCrn {

        private final String PATH_FORMAT = "/offenders/crn/%s/convictions/%s/courtAppearances";
        private final String CRN = "X320741";
        private final Long CONVICTION_ID = 2500295345L;

        @Test
        public void canGetCourtAppearancesForKnownCrnAndConvictionId() {

            var result = given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PATH_FORMAT, CRN, CONVICTION_ID))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtAppearanceBasicWrapper.class);

            List<CourtAppearanceBasic> appearances = result.getCourtAppearances();
            assertThat(appearances).hasSize(2);
            CourtAppearanceBasic appearance1 = appearances.get(0);
            assertThat(appearance1.getCourtCode()).isEqualTo("SHEFMC");
            assertThat(appearance1.getAppearanceDate()).isAfter(appearances.get(1).getAppearanceDate());
        }

        @Test
        public void givenUnknownCrn_whenGetCourtAppearances_thenReturn404() {

            given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PATH_FORMAT, "XXXXXX", CONVICTION_ID))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        public void canGetCourtAppearancesNoResults() {
            var result = given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PATH_FORMAT, CRN, 100L))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtAppearanceBasicWrapper.class);
            assertThat(result.getCourtAppearances()).hasSize(0);
        }

    }
}

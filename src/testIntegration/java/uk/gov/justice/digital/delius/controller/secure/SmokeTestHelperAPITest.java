package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class SmokeTestHelperAPITest extends IntegrationTestBase {
    @Nested
    @DisplayName("POST /offenders/nomsNumber/{nomsNumber}/smoketest/custody/reset")
    class NextUpdate {

        @Test
        @DisplayName("must have `ROLE_SMOKE_TEST` to access this service")
        public void mustHaveCommunityRole() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                    .auth().oauth2(token)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .post("/offenders/nomsNumber/{nomsNumber}/smoketest/custody/reset", "G4106UN")
                    .then()
                    .statusCode(403);
        }

        @Test
        @DisplayName("Can reset custody data")
        public void canGetNextUpdateWithDateChanged() {
            given()
                    .auth().oauth2(createJwt("ROLE_SMOKE_TEST"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .post("/offenders/nomsNumber/{nomsNumber}/smoketest/custody/reset", "G4106UN")
                    .then()
                    .statusCode(200);


        }
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RiskAPITest extends IntegrationTestBase {

    @Nested
    @TestInstance(PER_CLASS)
    class SecureEndpoints {

        @SuppressWarnings("unused")
        private Stream<Arguments> secureEndpoints() {
            return Stream.of(
                Arguments.of("/offenders/crn/CRN/risk/mappa")
            );
        }

        @ParameterizedTest
        @MethodSource("secureEndpoints")
        void noToken_unauthorised(String uri) {
            given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(uri)
                .then()
                .statusCode(401);
        }

        @ParameterizedTest
        @MethodSource("secureEndpoints")
        void missingRole_forbidden(String uri) {
            given()
                .auth().oauth2(createJwt())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(uri)
                .then()
                .statusCode(403);
        }
    }


    @Nested
    class Mappa {

        @Nested
        class OffenderFound {

            @Test
            void crnFound_ok() {
                given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/crn/X320741/risk/mappa")
                    .then()
                    .statusCode(200);
            }

            @Test
            void categoryAndLevelNull_okWithNominalValues() {
                given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/crn/X320811/risk/mappa")
                    .then()
                    .statusCode(200)
                    .body("level", equalTo(0))
                    .body("levelDescription", equalTo("Missing level"))
                    .body("category", equalTo(0))
                    .body("categoryDescription", equalTo("Missing category"));

            }

        }
    }
}

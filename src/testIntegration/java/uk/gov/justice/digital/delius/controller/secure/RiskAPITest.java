package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RiskAPITest extends IntegrationTestBase {

    @Nested
    @TestInstance(PER_CLASS)
    class SecureEndpoints {

        private Stream<Arguments> secureEndpoints() {
            return Stream.of(
                Arguments.of("/offenders/nomsNumber/NOMS/risk/mappa"),
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
    class NotFound {

        @Test
        void noOffender_notFound() {
            given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/UNKNOWN_OFFENDER/risk/mappa")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("developerMessage", equalTo("Offender not found"));
        }

        @Test
        void noMappaDetails_notFound() {
            given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9642VP/risk/mappa")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("developerMessage", equalTo("MAPPA details for offender not found"));
        }

    }

    @Nested
    class OffenderFound {

        @Test
        void nomsNumberFound_ok() {
            given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/risk/mappa")
                .then()
                .statusCode(200);
        }

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
        void mappaDetailsReturned() {
            given()
                .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/risk/mappa")
                .then()
                .statusCode(200)
                .body("level", equalTo(2))
                .body("levelDescription", equalTo("MAPPA Level 2"))
                .body("category", equalTo(2))
                .body("categoryDescription", equalTo("MAPPA Cat 2"))
                .body("startDate", equalTo(LocalDate.of(2021, 2, 1).format(ISO_LOCAL_DATE)))
                .body("reviewDate", equalTo(LocalDate.of(2021, 5, 1).format(ISO_LOCAL_DATE)))
                .body("team.code", equalTo("N02AAM"))
                .body("team.description", equalTo("OMIC OMU A "))
                .body("officer.code", equalTo("N02AAMU"))
                .body("officer.forenames", equalTo("Unallocated"))
                .body("officer.surname", equalTo("Staff"))
                .body("probationArea.code", equalTo("N02"))
                .body("probationArea.description", equalTo("NPS North East"))
                .body("notes", equalTo("X320741 registering MAPPA cat 2 level 2"));
        }

    }
}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
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

        @SuppressWarnings("unused")
        private Stream<Arguments> secureEndpoints() {
            return Stream.of(
                Arguments.of("/offenders/nomsNumber/NOMS/risk/mappa"),
                Arguments.of("/offenders/crn/CRN/risk/mappa"),
                Arguments.of("/offenders/nomsNumber/NOMS/risk/resourcing/latest"),
                Arguments.of("/offenders/crn/CRN/risk/resourcing/latest")
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
                    .get("/offenders/nomsNumber/G9643VP/risk/mappa")
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

    @Nested
    class Resourcing {
        @Nested
        class NotFound {

            @Test
            void noOffenderByNomsNumber_notFound() {
                given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/UNKNOWN_OFFENDER/risk/resourcing/latest")
                    .then()
                    .statusCode(404)
                    .body("status", equalTo(404))
                    .body("developerMessage", equalTo("Offender not found"));
            }

            @Test
            void noOffenderByCRNNumber_notFound() {
                given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/crn/UNKNOWN_OFFENDER/risk/resourcing/latest")
                    .then()
                    .statusCode(404)
                    .body("status", equalTo(404))
                    .body("developerMessage", equalTo("Offender not found"));
            }

            @Test
            void noCaseAllocation_notFound() {
                given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G9643VP/risk/resourcing/latest")
                    .then()
                    .statusCode(404)
                    .body("status", equalTo(404))
                    .body("developerMessage", equalTo("Resourcing details for offender not found"));
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
                    .get("/offenders/nomsNumber/G9542VP/risk/resourcing/latest")
                    .then()
                    .statusCode(200);
            }

            @Test
            void crnFound_ok() {
                given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/crn/X320741/risk/resourcing/latest")
                    .then()
                    .statusCode(200);
            }

            @Test
            void resourcingDetailsReturned() {
                given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G9542VP/risk/resourcing/latest")
                    .then()
                    .statusCode(200)
                    .body("decision.date", equalTo(LocalDate.of(2019, 9, 13).format(ISO_LOCAL_DATE)))
                    .body("decision.code", equalTo("R"))
                    .body("decision.description", equalTo("Retained"))
                    .body("relatedConvictionId", equalTo(2500295345L))
                    .body("enhancedResourcing", equalTo(true));
            }
        }
    }

    @Nested
    @DisplayName("When multiple records match the same noms number")
    class DuplicateNOMSNumbers{
        @Nested
        @DisplayName("When only one of the records is current")
        class OnlyOneActive{
            @Test
            @DisplayName("will return the absence of mappa details")
            void willSuccessfulIndicateWhenNoMappaDetails() {
                given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3232DD/risk/mappa")
                    .then()
                    .statusCode(404)
                    .body("status", equalTo(404))
                    .body("developerMessage", equalTo("MAPPA details for offender not found"));

            }
            @Test
            @DisplayName("will return the absence of resourcing details")
            void willSuccessfulIndicateWhenNoResourcingDetails() {
                given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3232DD/risk/resourcing/latest")
                    .then()
                    .statusCode(404)
                    .body("status", equalTo(404))
                    .body("developerMessage", equalTo("Resourcing details for offender not found"));

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
                    .get("/offenders/nomsNumber/G3636DD/risk/mappa")
                    .then()
                    .statusCode(409);
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3636DD/risk/resourcing/latest")
                    .then()
                    .statusCode(409);
            }
        }
    }
}

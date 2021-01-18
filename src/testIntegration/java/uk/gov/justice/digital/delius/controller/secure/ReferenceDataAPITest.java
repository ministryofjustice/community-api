package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.KeyValue;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(SpringExtension.class)
public class ReferenceDataAPITest extends IntegrationTestBase {
    @Nested
    class GetReferenceData {
        @Test
        public void mustHaveCommunityRole() {
            final var token = createJwt("ROLE_BANANAS");

            given()
                    .auth().oauth2(token)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/referenceData/set/{set}", "ADDITIONAL IDENTIFIER TYPE")
                    .then()
                    .statusCode(403);
        }

        @Test
        public void willGet404IfDataSetNotFound() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                    .auth().oauth2(token)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/referenceData/set/{set}", "BANANAS")
                    .then()
                    .statusCode(404);
        }

        @Test
        public void willReturnDataInTheSuppliedDataSet() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                    .auth().oauth2(token)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/referenceData/set/{set}", "ADDITIONAL IDENTIFIER TYPE")
                    .then()
                    .statusCode(200)
                    // currently 46 different identifiers included the inactive ones in seed data
                    .body("referenceData.size()", is(46))
                    .body("referenceData.find { it.code == 'VISO' }.description", is("ViSOR Number"))
                    .body("referenceData[0].code", notNullValue())
                    .body("referenceData[0].description", notNullValue());
        }
    }

    @Nested
    class GetReferenceDataSets {
        @Test
        public void mustHaveCommunityRole() {
            final var token = createJwt("ROLE_BANANAS");

            given()
                    .auth().oauth2(token)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/referenceData/sets")
                    .then()
                    .statusCode(403);
        }

        @Test
        public void willReturnAllDataSets() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                    .auth().oauth2(token)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/referenceData/sets")
                    .then()
                    .statusCode(200)
                    // currently 157 different codes sets in seed data
                    .body("referenceDataSets.size()", is(157))
                    .body("referenceDataSets.find { it.code == 'ADDITIONAL IDENTIFIER TYPE' }.description", is("Additional Identifier Type"))
                    .body("referenceDataSets[0].code", notNullValue())
                    .body("referenceDataSets[0].description", notNullValue());

        }

    }

    @Nested
    class ProbationAreas {
        private static final String ACTIVE_PROBATION_AREA = "N02";
        private static final String INACTIVE_PROBATION_AREA = "BED";
        private static final String ACTIVE_PRISON = "MDI";
        private static final String EXISTING_LDU = "YSS_SHF";
        private static final String MISSING_LDU = "NOT_EXISTING";

        @Test
        public void canGetAllProbationAreas() {
            var probationAreas = given()
                    .when()
                    .auth()
                    .oauth2(createJwt("ROLE_COMMUNITY"))
                    .get("/probationAreas")
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath().getList("content", KeyValue.class);

            assertThat(probationAreas)
                    .hasSize(248)
                    .extracting("code")
                    .contains(ACTIVE_PROBATION_AREA)
                    .contains(INACTIVE_PROBATION_AREA);
        }

        @Test
        public void canGetOnlyActiveProbationAreas() {
            var probationAreas = given()
                    .when()
                    .auth()
                    .oauth2(createJwt("ROLE_COMMUNITY"))
                    .param("active", true)
                    .get("/probationAreas")
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath().getList("content", KeyValue.class);

            assertThat(probationAreas)
                    .hasSize(176)
                    .extracting("code")
                    .contains(ACTIVE_PROBATION_AREA, ACTIVE_PRISON)
                    .doesNotContain(INACTIVE_PROBATION_AREA);
        }

        @Test
        public void canGetOnlyActiveProbationAreasExcludingPrisons() {
            var probationAreas = given()
                    .when()
                    .auth()
                    .oauth2(createJwt("ROLE_COMMUNITY"))
                    .param("active", true)
                    .param("excludeEstablishments", true)
                    .get("/probationAreas")
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath().getList("content", KeyValue.class);

            assertThat(probationAreas)
                    .hasSize(45)
                    .extracting("code")
                    .contains(ACTIVE_PROBATION_AREA)
                    .doesNotContain(INACTIVE_PROBATION_AREA, ACTIVE_PRISON);
        }

        @Test
        public void willDefaultToNotExcludePrisonsProbationAreas() {
            var allProbationAreas = given()
                    .when()
                    .auth()
                    .oauth2(createJwt("ROLE_COMMUNITY"))
                    .get("/probationAreas")
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath().getList("content", KeyValue.class);

            var allProbationAreasIncludingEstablishments = given()
                    .when()
                    .auth()
                    .oauth2(createJwt("ROLE_COMMUNITY"))
                    .param("excludeEstablishments", false)
                    .get("/probationAreas")
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath().getList("content", KeyValue.class);

            assertThat(allProbationAreas)
                    .isEqualTo(allProbationAreasIncludingEstablishments)
                    .hasSize(248)
                    .extracting("code")
                    .contains(ACTIVE_PROBATION_AREA, ACTIVE_PRISON, INACTIVE_PROBATION_AREA);
        }


        @Test
        public void canGetLocalDeliveryUnits() {
            List<KeyValue> localDeliveryUnits = given()
                    .when()
                    .auth()
                    .oauth2(createJwt("ROLE_COMMUNITY"))
                    .get(String.format("/probationAreas/code/%s/localDeliveryUnits", ACTIVE_PROBATION_AREA))
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath().getList("content", KeyValue.class);

            assertThat(localDeliveryUnits).extracting("code").contains(EXISTING_LDU, "N02LEE", "N02NNT", "N02SDL");
        }

        @Test
        public void canGetLocalDeliveryUnits_missingProbationArea() {
            ErrorResponse response = given()
                    .when()
                    .auth()
                    .oauth2(createJwt("ROLE_COMMUNITY"))
                    .get("/probationAreas/code/A_MISSING_PROBATION_AREA/localDeliveryUnits")
                    .then()
                    .statusCode(404)
                    .extract()
                    .body()
                    .as(ErrorResponse.class);

            assertThat(response).isEqualTo(ErrorResponse.builder()
                    .status(404)
                    .developerMessage("Could not find probation area with code: 'A_MISSING_PROBATION_AREA'")
                    .build());
        }

        @Test
        public void canGetTeamsForDeliveryUnit() {
            List<KeyValue> localDeliveryUnits = given()
                    .when()
                    .auth()
                    .oauth2(createJwt("ROLE_COMMUNITY"))
                    .get(String
                            .format("/probationAreas/code/%s/localDeliveryUnits/code/%s/teams", ACTIVE_PROBATION_AREA, EXISTING_LDU))
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath().getList("content", KeyValue.class);

            assertThat(localDeliveryUnits).extracting("code").containsOnly("N02N30", "N02N21");
        }

        @Test
        public void canGetTeamsForDeliveryUnit_missingLocalDeliveryUnit() {
            ErrorResponse response = given()
                    .when()
                    .auth()
                    .oauth2(createJwt("ROLE_COMMUNITY"))
                    .get(String
                            .format("/probationAreas/code/%s/localDeliveryUnits/code/%s/teams", ACTIVE_PROBATION_AREA, MISSING_LDU))
                    .then()
                    .statusCode(404)
                    .extract()
                    .body()
                    .as(ErrorResponse.class);

            assertThat(response).isEqualTo(ErrorResponse.builder()
                    .status(404)
                    .developerMessage(String
                            .format("Could not find local delivery unit in probation area: '%s', with code: '%s'", ACTIVE_PROBATION_AREA, MISSING_LDU))
                    .build());
        }

        @Nested
        class ProbationAreasAndLocalDeliveryUnits {
            private static final String ACTIVE_PROBATION_AREA = "N01";
            private static final String INACTIVE_PROBATION_AREA = "BED";
            private static final String ACTIVE_PRISON = "MDI";
            private static final String EXISTING_LDU = "YSS_SHF";
            private static final String MISSING_LDU = "NOT_EXISTING";

            @Test
            public void canGetActiveProbationAreasAndLocalDeliveryUnits() {
                given()
                        .when()
                        .auth()
                        .oauth2(createJwt("ROLE_COMMUNITY"))
                        .param("active", true)
                        //should we be including this parameter - why are does probation area include prisons without?
                        .get("/probationAreas/localDeliveryUnits")
                        .then()
                        .assertThat()
                        .body("size()", is(45))
                        .body(String.format("find { it.code == \"%s\" }.localDeliveryUnits.size()",ACTIVE_PROBATION_AREA), is(72))
                        .body(String.format("find { it.code == \"%s\" }.localDeliveryUnits[0].code",ACTIVE_PROBATION_AREA), notNullValue())
                        .body(String.format("find { it.code == \"%s\" }",INACTIVE_PROBATION_AREA), nullValue());
            }

            @Test
            public void canGetAllProbationAreasAndLocalDeliveryUnits() {
                given()
                        .when()
                        .auth()
                        .oauth2(createJwt("ROLE_COMMUNITY"))
                        .get("/probationAreas/localDeliveryUnits")
                        .then()
                        .assertThat()
                        .body("size()", is(112))
                        .body(String.format("find { it.code == \"%s\" }",INACTIVE_PROBATION_AREA), notNullValue());
            }
        }
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.data.api.KeyValue;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class ReferenceDataAPITest extends IntegrationTestBase {

    @Nested
    class ProbationAreas {
        private static final String ACTIVE_PROBATION_AREA = "N02";
        private static final String INACTIVE_PROBATION_AREA = "BED";
        private static final String ACTIVE_PRISON = "MDI";

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
                    .hasSize(249)
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
                    .hasSize(177)
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
                    .hasSize(46)
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
                    .hasSize(249)
                    .extracting("code")
                    .contains(ACTIVE_PROBATION_AREA, ACTIVE_PRISON, INACTIVE_PROBATION_AREA);
        }
    }
}

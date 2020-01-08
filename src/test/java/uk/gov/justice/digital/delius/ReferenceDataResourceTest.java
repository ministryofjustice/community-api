package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.LocalDeliveryUnit;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
public class ReferenceDataResourceTest {

    public static final String ACTIVE_PROBATION_AREA = "TES";
    public static final String INACTIVE_PROBATION_AREA = "BED";
    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.good}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void canGetAllProbationAreas() {
        var probationAreas = given()
                .when()
                .auth()
                .oauth2(validOauthToken)
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
                .oauth2(validOauthToken)
                .param("active", true)
                .get("/probationAreas")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList("content", KeyValue.class);

        assertThat(probationAreas)
                .hasSize(208)
                .extracting("code")
                .contains(ACTIVE_PROBATION_AREA)
                .doesNotContain(INACTIVE_PROBATION_AREA);
    }

    @Test
    public void canGetLocalDeliveryUnits() {
        List<KeyValue> localDeliveryUnits = given()
                .when()
                .auth()
                .oauth2(validOauthToken)
                .get(String.format("/probationAreas/code/%s/localDeliveryUnits", ACTIVE_PROBATION_AREA))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList("content", KeyValue.class);

        assertThat(localDeliveryUnits).extracting("code").containsOnly("TESMIG", "TESUAT", "TESSPG");
    }

    @Test
    public void canGetLocalDeliveryUnits_missingProbationArea() {
        ErrorResponse response = given()
                .when()
                .auth()
                .oauth2(validOauthToken)
                .get("/probationAreas/code/A_MISSING_PROBATION_AREA/localDeliveryUnits")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(response).isEqualTo(ErrorResponse.builder()
                .status(404)
                .developerMessage("Probation area with code A_MISSING_PROBATION_AREA")
                .build());
    }

}

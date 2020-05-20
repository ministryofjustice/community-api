package uk.gov.justice.digital.delius.controller.secure;

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

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
public class ReferenceDataResourceTest {

    private static final String ACTIVE_PROBATION_AREA = "N02";
    private static final String INACTIVE_PROBATION_AREA = "BED";
    private static final String ACTIVE_PRISON = "MDI";
    private static final String EXISTING_LDU = "YSS_SHF";
    private static final String MISSING_LDU = "NOT_EXISTING";

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
                .oauth2(validOauthToken)
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
                .oauth2(validOauthToken)
                .get("/probationAreas")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList("content", KeyValue.class);

        var allProbationAreasIncludingEstablishments = given()
                .when()
                .auth()
                .oauth2(validOauthToken)
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
                .oauth2(validOauthToken)
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
                .oauth2(validOauthToken)
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
                .oauth2(validOauthToken)
                .get(String.format("/probationAreas/code/%s/localDeliveryUnits/code/%s/teams", ACTIVE_PROBATION_AREA, EXISTING_LDU))
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
                .oauth2(validOauthToken)
                .get(String.format("/probationAreas/code/%s/localDeliveryUnits/code/%s/teams", ACTIVE_PROBATION_AREA, MISSING_LDU))
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(response).isEqualTo(ErrorResponse.builder()
                .status(404)
                .developerMessage(String.format("Could not find local delivery unit in probation area: '%s', with code: '%s'", ACTIVE_PROBATION_AREA, MISSING_LDU))
                .build());
    }
}
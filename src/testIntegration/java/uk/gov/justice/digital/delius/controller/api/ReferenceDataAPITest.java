package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class ReferenceDataAPITest {

    @LocalServerPort
    int port;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Jwt jwt;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    private ProbationArea aProbationArea(int i) {
        return ProbationArea.builder()
                .code("P0" + i)
                .description("Probation" + i)
                .build();
    }

    private List<ProbationArea> someProbationAreas() {
        return ImmutableList.of(
                aProbationArea(1),
                aProbationArea(2));
    }

    @Test
    public void canGetAllProbationAreas() {
        ProbationArea[] probationAreas = given()
                .when()
                .header("Authorization", aValidToken())
                .get("/probationAreas")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ProbationArea[].class);

        assertThat(probationAreas).extracting(ProbationArea::getCode).contains("C01", "C02", "C03");
    }

    @Test
    public void canGetProbationAreasForCode() {
        ProbationArea[] probationAreas = given()
                .when()
                .header("Authorization", aValidToken())
                .get("/probationAreas/code/C01")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ProbationArea[].class);

        assertThat(probationAreas).extracting("code").containsOnly("C01");
    }

    private String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }


}

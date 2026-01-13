package uk.gov.justice.digital.delius.controller.api;

import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.mapper.ObjectMapperType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
@Import(uk.gov.justice.digital.delius.test.FlywayKickConfig.class)
public class ReferenceDataAPITest {

    @LocalServerPort
    int port;
    @Autowired

    private JsonMapper jsonMapper;
    @Autowired
    private Jwt jwt;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig(ObjectMapperType.JACKSON_3)
            .jackson3ObjectMapperFactory(
                (aClass, s) -> jsonMapper
            ));
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

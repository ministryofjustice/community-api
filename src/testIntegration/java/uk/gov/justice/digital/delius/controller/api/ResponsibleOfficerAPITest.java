package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class ResponsibleOfficerAPITest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @Test
    public void getCurrentRoForOffender() {

        ResponsibleOfficer[] responsibleOfficers =
                given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/nomsNumber/G0560UO/responsibleOfficers?current=true")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ResponsibleOfficer[].class);

        List<ResponsibleOfficer> listOfRos = Arrays.asList(responsibleOfficers);
        assertThat(listOfRos).hasSize(1);
    }

    @Test
    public void getAllRosForOffender() {

        ResponsibleOfficer[] responsibleOfficers =
                given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/nomsNumber/G4106UN/responsibleOfficers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ResponsibleOfficer[].class);

        List<ResponsibleOfficer> listOfRos = Arrays.asList(responsibleOfficers);
        assertThat(listOfRos).hasSize(1);
    }

    @Test
    public void invalidOffenderNotFound() {

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/nomsNumber/G9999NN/responsibleOfficers")
                .then()
                .statusCode(404);
    }

    @Test
    public void noRosReturnEmptyList() {

        ResponsibleOfficer[] responsibleOfficers =
                given()
                .header("Authorization", aValidToken())
                .when()
                .get("/offenders/nomsNumber/G8331VA/responsibleOfficers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ResponsibleOfficer[].class);

        List<ResponsibleOfficer> listOfRos = Arrays.asList(responsibleOfficers);
        assertThat(listOfRos).isEmpty();
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

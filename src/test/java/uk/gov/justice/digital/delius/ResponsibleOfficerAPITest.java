package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficer;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class ResponsibleOfficerAPITest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    OffenderService offenderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @Test
    public void getCurrentRoForOffender() {

        ResponsibleOfficer[] responsibleOfficers =
                given()
                .when()
                .get("/offenders/nomsNumber/AA111/responsibleOfficers?current=true")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ResponsibleOfficer[].class);

        List<ResponsibleOfficer> listOfRos = Arrays.asList(responsibleOfficers);
        assertThat(listOfRos).hasSize(1);

        // TODO: check the content of attributes here around the current responsible officers
    }

    @Test
    public void getAllRosForOffender() {

        ResponsibleOfficer[] responsibleOfficers =
                given()
                .when()
                .get("/offenders/nomsNumber/AA111/responsibleOfficers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ResponsibleOfficer[].class);

        List<ResponsibleOfficer> listOfRos = Arrays.asList(responsibleOfficers);
        assertThat(listOfRos).hasSize(2);

        // TODO: check the content of attributes here around the historical responsible officers
    }

    @Test
    public void invalidOffenderNotFound() {

        ResponsibleOfficer[] responsibleOfficers =
                given()
                .when()
                .get("/offenders/nomsNumber/AA999/responsibleOfficers")
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ResponsibleOfficer[].class);

        List<ResponsibleOfficer> listOfRos = Arrays.asList(responsibleOfficers);
        assertThat(listOfRos).isEmpty();
    }

    @Test
    public void noRosReturnEmptyList() {

        ResponsibleOfficer[] responsibleOfficers =
                given()
                .when()
                .get("/offenders/nomsNumber/AA333/responsibleOfficers")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(ResponsibleOfficer[].class);

        List<ResponsibleOfficer> listOfRos = Arrays.asList(responsibleOfficers);
        assertThat(listOfRos).isEmpty();
    }
}
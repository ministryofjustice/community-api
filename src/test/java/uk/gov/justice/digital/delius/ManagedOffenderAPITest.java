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
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class ManagedOffenderAPITest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    OffenderService staffService;

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
    public void getCurrentManagedOffendersForOfficer() {

        ManagedOffender[] managedOffenders =
                given()
                        .when()
                        .get("/staff/stafffCode/ST111/managedOffenders?current=true")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).hasSize(1);

        // TODO: check the content of attributes here around the current offenders
    }

    @Test
    public void getAllManagedOffendersForOfficer() {

        ManagedOffender[] managedOffenders =
                given()
                        .when()
                        .get("/staff/stafffCode/ST111/managedOffenders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).hasSize(2);

        // TODO: check the content of attributes here around historically managed offenders

    }

    @Test
    public void getInvalidOfficerNotFound() {

        ManagedOffender[] managedOffenders =
                given()
                        .when()
                        .get("/staff/stafffCode/ST999/managedOffenders")
                        .then()
                        .statusCode(404)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).isEmpty();
    }

    @Test
    public void getUnassignedOfficerEmptyList() {

        ManagedOffender[] managedOffenders =
                given()
                        .when()
                        .get("/staff/stafffCode/ST333/managedOffenders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).isEmpty();
    }
}
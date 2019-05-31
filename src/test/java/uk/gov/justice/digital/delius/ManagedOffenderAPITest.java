package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.parsing.Parser;
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
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

    @Autowired
    private Jwt jwt;


    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @Test
    public void getCurrentManagedOffendersForOfficer() {

        ManagedOffender[] managedOffenders =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/staff/staffCode/SH0001/managedOffenders?current=true")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).hasSize(3);
    }

    @Test
    public void getAllManagedOffendersForOfficer() {

        ManagedOffender[] managedOffenders =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/staff/staffCode/SH0001/managedOffenders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).hasSize(3);
    }

    @Test
    public void getInvalidOfficerNotFound() {

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("/staff/staffCode/SH9999/managedOffenders")
            .then()
            .statusCode(404);
    }

    @Test
    public void getUnassignedOfficerEmptyList() {

        ManagedOffender[] managedOffenders =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/staff/staffCode/SH0006/managedOffenders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).isEmpty();
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
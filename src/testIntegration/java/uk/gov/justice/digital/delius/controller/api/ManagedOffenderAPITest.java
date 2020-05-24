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
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class ManagedOffenderAPITest {

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
    public void getCurrentManagedOffendersForOfficer() {

        /*
         This officer has four offenders assigned in seed data.
         Three are current and one is soft_deleted so only 3 managed offenders expected.
         */

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
    public void getManagedOffendersSoftDeletedCheck() {

        /*
         This officer has two offenders assigned but one is SOFT_DELETED in seed data.
         The response should be only one managed offender - G3232VA.
        */

        ManagedOffender[] managedOffenders =
                given()
                        .header("Authorization", aValidToken())
                        .when()
                        .get("/staff/staffCode/SH0008/managedOffenders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).hasSize(1);
        assertThat(mos.get(0).getNomsNumber()).isEqualToIgnoringCase("G3232VA");
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

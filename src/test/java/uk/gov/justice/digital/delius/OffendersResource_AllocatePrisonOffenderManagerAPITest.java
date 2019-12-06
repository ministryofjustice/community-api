package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
public class OffendersResource_AllocatePrisonOffenderManagerAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Flyway flyway;

    @Autowired
    private Jwt jwt;


    @Value("${test.token.good}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @After
    public void after() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void shouldRespondWith404WhenAllocatingPrisonOffenderManagersAndExistingStaffNotFound() throws JsonProcessingException {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("DOESNOTEXIST"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldRespondWith404WhenAllocatingPrisonOffenderManagersAndOffenderNotFound() throws JsonProcessingException {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("BWIA010"))
                .when()
                .put("/offenders/nomsNumber/DOESNOTEXIST/prisonOffenderManager")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldRespondWith404WhenAllocatingPrisonOffenderManagersAndPrisonInstitutionNotFound() throws JsonProcessingException {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("BWIA010", "DOESNOTEXIST"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldRespondWith400WhenStaffMemberNotInThePrisonInstitutionProbationArea() throws JsonProcessingException {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .contentType("application/json")
                .body(createPrisonOffenderManagerOf("BWIA010", "WWI"))
                .when()
                .put("/offenders/nomsNumber/G9542VP/prisonOffenderManager")
                .then()
                .statusCode(400);
    }

    private String createPrisonOffenderManagerOf(String staffCode) throws JsonProcessingException {
        return objectMapper.writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .officerCode(staffCode)
                .nomsPrisonInstitutionCode("BWI")
                .build());
    }

    private String createPrisonOffenderManagerOf(String staffCode, String nomsPrisonInstitutionCode) throws JsonProcessingException {
        return objectMapper.writeValueAsString(CreatePrisonOffenderManager
                .builder()
                .officerCode(staffCode)
                .nomsPrisonInstitutionCode(nomsPrisonInstitutionCode)
                .build());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }


}

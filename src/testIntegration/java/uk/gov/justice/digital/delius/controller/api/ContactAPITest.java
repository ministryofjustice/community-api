package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class ContactAPITest {

    private final LocalDateTime now = LocalDateTime.now();
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

    @Test
    // TODO DT-835 This test intermittently fails due to data being corrupted by another test - currently working in Circle by splitting the tests into parallel executions, but needs a proper fix
    public void canGetAllContactsByOffenderId() {
        Contact[] contacts = given()
                .when()
                .header("Authorization", aValidToken())
                .get("/offenders/crn/X320741/contacts")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Contact[].class);

        assertThat(contacts).extracting("contactId").contains(2502743375L); // PLus millions of others, but we're testing that e2e stands up and the details are covered in the unit test
    }

    @Test
    public void contactsByCrnMustHaveValidJwt() {
        given()
                .when()
                .queryParam("to", now.toString())
                .get("/offenders/crn/crn1/contacts")
                .then()
                .statusCode(401);

    }

    @Test
    public void contactsByNomsNumberMustHaveVaidJwt() {
        given()
                .when()
                .queryParam("to", now.toString())
                .get("/offenders/nomsNumber/noms1/contacts")
                .then()
                .statusCode(401);

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

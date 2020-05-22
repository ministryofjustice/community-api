package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.JwtAuthenticationHelper;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class RegistrationsAPITest {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String OFFENDER_ID = "2500343964";
    private static final String CRN = "X320741";
    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;
    @LocalServerPort
    int port;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void mustHaveCommunityRole() {
        final var token = createJwt("ROLE_BANANAS");

        given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/offenderId/{offenderId}/registrations", OFFENDER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    public void canGetRegistrationByOffenderId() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/offenderId/{offenderId}/registrations", OFFENDER_ID)
                .then()
                .statusCode(200)
                .body("registrations[0].register.description", is("Public Protection"))
                .body("registrations[0].startDate", is("2019-10-11"));

    }

    @Test
    public void canGetRegistrationByNOMSNumber() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/{nomsNumber}/registrations", NOMS_NUMBER)
                .then()
                .statusCode(200)
                .body("registrations[0].register.description", is("Public Protection"));
    }

    @Test
    public void canGetRegistrationByCRN() {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/registrations", CRN)
                .then()
                .statusCode(200)
                .body("registrations[0].register.description", is("Public Protection"));
    }

    private String createJwt(final String... roles) {
        return jwtAuthenticationHelper.createJwt(JwtAuthenticationHelper.JwtParameters.builder()
                .username("APIUser")
                .roles(List.of(roles))
                .scope(Arrays.asList("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }
}

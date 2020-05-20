package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Attendances;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
@RunWith(SpringJUnit4ClassRunner.class)
public class AttendanceResourceAPITest {

    private static final Long KNOWN_EVENT_ID = 2500295343L;
    private static final String KNOWN_CRN = "X320741";
    private static final String PATH_FORMAT = "/offenders/crn/%s/convictions/%s/attendances";
    private static final String PATH = String.format(PATH_FORMAT, KNOWN_CRN, KNOWN_EVENT_ID);

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.good}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void normalGetAttendances() {
        final Attendances attendances = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(Attendances.class);

        assertThat(attendances.getAttendances().stream()).hasSize(3);
    }

    @Test
    public void getBadRequest400() {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(PATH_FORMAT, "XXX", "XXX"))
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void getKnownCrnButEventIdNotFound200() {
        final String eventId = "923213723";
        final Attendances attendances = given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(PATH_FORMAT, KNOWN_CRN, eventId))
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(Attendances.class);

        assertTrue(attendances.getAttendances().isEmpty());
    }

}
package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.Offence;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class OffendersResource_getOffenderConvictionsByCrn {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.good}")
    private String validOauthToken;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void canGetOffenderConvictionsByCrn() {
        final var convictions = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/convictions")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Conviction[].class);

        final var conviction = Stream.of(convictions).filter(Conviction::getActive).findAny().orElseThrow();
        final var offence = conviction.getOffences().stream().filter(Offence::getMainOffence).findAny().orElseThrow();
        assertThat(offence.getDetail().getCode()).isEqualTo("00102");
    }

    @Test
    public void canGetOffenderConvictionByCrnAndConvictionId() {
        final var conviction = given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/convictions/2500295343")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Conviction.class);

        final var offence = conviction.getOffences().stream().filter(Offence::getMainOffence).findAny().orElseThrow();
        assertThat(offence.getDetail().getCode()).isEqualTo("05600");
    }

    @Test
    public void convictionsHaveAssociatedUnpaidWorkData() {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/convictions")
                .then()
                .statusCode(200)
                .body("[2].sentence.unpaidWork.minutesOrdered", equalTo(3600))
                .body("[2].sentence.unpaidWork.minutesCompleted", equalTo(360))
                .body("[2].sentence.unpaidWork.appointments.total", equalTo(5))
                .body("[2].sentence.unpaidWork.appointments.attended", equalTo(2))
                .body("[2].sentence.unpaidWork.appointments.acceptableAbsences", equalTo(1))
                .body("[2].sentence.unpaidWork.appointments.unacceptableAbsences", equalTo(1))
                .body("[2].sentence.unpaidWork.appointments.noOutcomeRecorded", equalTo(1));
    }

    @Test
    public void getOffenderConvictionsByCrn_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X777777/convictions")
                .then()
                .statusCode(404);
    }
}

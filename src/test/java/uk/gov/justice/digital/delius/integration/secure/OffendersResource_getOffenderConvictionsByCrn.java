package uk.gov.justice.digital.delius.integration.secure;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.Offence;

import java.time.LocalDate;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
@RunWith(SpringJUnit4ClassRunner.class)
public class OffendersResource_getOffenderConvictionsByCrn {

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
    public void convictionsHaveAssociatedUnpaidWorkData() {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/convictions")
                .then()
                .statusCode(200)
                .body("[2].sentence.unpaidWork.unpaidWork.minutesOrdered", equalTo(12*60))
                .body("[2].sentence.unpaidWork.unpaidWork.minutesCompleted", equalTo(4*60))
                .body("[2].sentence.unpaidWork.unpaidWork.appointments.total", equalTo(10))
                .body("[2].sentence.unpaidWork.unpaidWork.appointments.attended", equalTo(4))
                .body("[2].sentence.unpaidWork.unpaidWork.appointments.acceptableAbsences", equalTo(3))
                .body("[2].sentence.unpaidWork.unpaidWork.appointments.unacceptableAbsences", equalTo(2))
                .body("[2].sentence.unpaidWork.unpaidWork.appointments.noOutcomeRecorded", equalTo(1))
                .body("[2].sentence.unpaidWork.unpaidWork.asOf", equalTo(LocalDate.now()));
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

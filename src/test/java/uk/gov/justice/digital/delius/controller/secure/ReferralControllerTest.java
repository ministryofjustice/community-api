
package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralEndRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralStartRequest;
import uk.gov.justice.digital.delius.service.ReferralService;

import java.time.OffsetDateTime;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


public class ReferralControllerTest {

    private ReferralService referralService = mock(ReferralService.class);
    private static final String SOME_OFFENDER_CRN = "X0OOM";

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
            new ReferralController(referralService),
            new SecureControllerAdvice()
        );
    }

    @Test
    public void createReferral_returnsBadRequestWhenNoBodySupplied() {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body("")
            .when()
            .post(String.format("/secure/offenders/crn/%s/referral/start/context/commissioned-rehabilitation-services", SOME_OFFENDER_CRN))
            .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    public void startReferral_callsServiceAndReturnsOKWhenValidationSucceeds() {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(ContextlessReferralStartRequest.builder()
                .startedAt(OffsetDateTime.now())
                .contractType("ACC")
                .sentenceId(12354L)
                .notes("comes notes")
                .build()
            )
            .when()
            .post(String.format("/secure/offenders/crn/%s/referral/start/context/commissioned-rehabilitation-services", SOME_OFFENDER_CRN))
            .then()
            .statusCode(200);
    }

    @Test
    public void endReferral_callsServiceAndReturnsOKWhenValidationSucceeds() {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(ContextlessReferralEndRequest.builder()
                .startedAt(OffsetDateTime.now())
                .endedAt(OffsetDateTime.now())
                .contractType("ACC")
                .sentenceId(12354L)
                .endType("COMPLETED")
                .notes("comes notes")
                .build()
            )
            .when()
            .post(String.format("/secure/offenders/crn/%s/referral/start/context/commissioned-rehabilitation-services", SOME_OFFENDER_CRN))
            .then()
            .statusCode(200);
    }
}

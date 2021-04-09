
package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.service.ReferralService;

import java.time.LocalDate;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


public class ReferralControllerTest {

    private ReferralService referralService = mock(ReferralService.class);
    private static final String SOME_OFFENDER_CRN = "X0OOM";
    private static final String INTEGRATION_CONTEXT = "commissioned-rehabilitation-services";

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
            new ReferralController(referralService),
            new SecureControllerAdvice()
        );
    }

    @Test
    public void createReferral_returnsBadRequestWhenNoBodySupplied() throws JsonProcessingException {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body("")
            .when()
            .post(String.format("/secure/offenders/crn/%s/referral/sent", SOME_OFFENDER_CRN))
            .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    public void updateReferral_callsServiceAndReturnsOKWhenValidationSucceeds() throws JsonProcessingException {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(ReferralSentRequest.builder()
                .date(LocalDate.now())
                .serviceCategory("Dependency and Recovery (D&R)")
                .sentenceId(12354L)
                .notes("comes notes")
                .context(INTEGRATION_CONTEXT)
                .build()
            )
            .when()
            .post(String.format("/secure/offenders/crn/%s/referral/sent", SOME_OFFENDER_CRN))
            .then()
            .statusCode(200);
    }
}

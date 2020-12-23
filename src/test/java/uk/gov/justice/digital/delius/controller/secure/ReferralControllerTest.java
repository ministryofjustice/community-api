package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.service.ReferralService;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


public class ReferralControllerTest {

    private ReferralService referralService = mock(ReferralService.class);
    private static final Long SOME_OFFENDER_ID = 3000L;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.config =  newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
            new ReferralController(referralService),
            new SecureControllerAdvice()
        );
    }

    @Test
    public void createReferral_returnsBadRequest() throws JsonProcessingException {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body("{}")
            .when()
            .put("/secure/referrals/sent/noOffenderID")
            .then()
            .statusCode(400);
    }

    @Test
    public void updateReferral_returnsOK() throws JsonProcessingException {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body("{}")
            .when()
            .put(String.format("/secure/referrals/sent/%s", SOME_OFFENDER_ID))
            .then()
            .statusCode(200);
    }
}

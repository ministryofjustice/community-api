
package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.ContextlessNotificationCreateRequest;
import uk.gov.justice.digital.delius.service.NotificationService;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static java.lang.String.format;
import static java.time.OffsetDateTime.now;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class NotificationControllerTest {

    private NotificationService notificationService = mock(NotificationService.class);
    private static final String SOME_OFFENDER_CRN = "X0OOM";
    private static final Long SOME_SENTENCE_ID = 123456L;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
            new NotificationController(notificationService),
            new SecureControllerAdvice()
        );
    }

    @Test
    public void createNotification_returnsBadRequestWhenNoBodySupplied() {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body("")
            .when()
            .post(format("/secure/offenders/crn/%s/sentences/%d/notifications/context/commissioned-rehabilitation-services",
                SOME_OFFENDER_CRN, SOME_SENTENCE_ID))
            .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    public void createNotification_callsServiceAndReturnsOKWhenValidationSucceeds() {

        ContextlessNotificationCreateRequest request = ContextlessNotificationCreateRequest.builder()
            .contractType("ACC")
            .referralStart(now())
            .contactDateTime(now())
            .notes("comes notes")
            .build();

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(format("/secure/offenders/crn/%s/sentences/%d/notifications/context/commissioned-rehabilitation-services",
                SOME_OFFENDER_CRN, SOME_SENTENCE_ID))
            .then()
            .statusCode(200);
    }
}

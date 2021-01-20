package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.gov.justice.digital.delius.JwtParameters;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;

import static io.restassured.RestAssured.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

public class ReferralTest extends IntegrationTestBase {

    @Test
    @DisplayName("Will add referral")
    void willAddReferral() {
        given()
            .auth()
            .oauth2(jwt())
            .contentType(APPLICATION_JSON_VALUE)
            .body(createReferralSent())
            .when()
            .post("offenders/crn/X320741/referral/sent")
            .then()
            .statusCode(200);
    }

    private String jwt() {
        return jwtAuthenticationHelper
            .createJwt(JwtParameters.builder()
                       .roles(List.of("ROLE_COMMUNITY"))
                       .scope(Arrays.asList("read", "write"))
                       .expiryTime(Duration.ofDays(1))
                       .clientId("system-client-id")
                       .build());
    }

    private String createReferralSent() {
        return writeValueAsString(ReferralSentRequest
                                  .builder()
                                  .probationAreaCode("YSS")
                                  .referralType("C116")
                                  .staffCode("N06AAFU")
                                  .teamCode("N05MKU")
                                  .date(LocalDate.now())
                                  .build());
    }
}

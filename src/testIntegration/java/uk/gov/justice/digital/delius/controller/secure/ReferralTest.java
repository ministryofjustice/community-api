package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.gov.justice.digital.delius.JwtParameters;
import uk.gov.justice.digital.delius.data.api.ReferralSent;

import static io.restassured.RestAssured.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.time.Duration;
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
            .put("referrals/sent/3000")
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
        return writeValueAsString(ReferralSent
                                  .builder()
                                  .probationArea("51")
                                  .contactType("418")
                                  .providerTeam("2500000000")
                                  .probationOfficer("2500000000")
                                  .employeeId("2500000000")
                                  .context("2500000000")
                                  .build());
    }
}

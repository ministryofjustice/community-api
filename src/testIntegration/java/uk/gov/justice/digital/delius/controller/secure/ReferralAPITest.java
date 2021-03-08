package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.RestAssured;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.JwtAuthenticationHelper;
import uk.gov.justice.digital.delius.JwtParameters;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusApiExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusApiMockServer;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class ReferralAPITest extends IntegrationTestBase {

    private static final DeliusApiMockServer deliusApiMockServer = new DeliusApiMockServer(7999);

    @RegisterExtension
    static DeliusApiExtension deliusExtension = new DeliusApiExtension(deliusApiMockServer);

    @LocalServerPort
    int port;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
    }

    @Test
    public void shouldReturnOKWhenSendContactToDeliusApi() {

        deliusApiMockServer.stubPostContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        final var response = given()
                .when()
                .auth().oauth2(token)
                .contentType(String.valueOf(ContentType.APPLICATION_JSON))
                .body(writeValueAsString(ReferralSentRequest
                    .builder()
                    .providerCode("YSS")
                    .referralType("C116")
                    .staffCode("N06AAFU")
                    .teamCode("N05MKU")
                    .date(LocalDate.now())
                    .notes("A test note")
                    .build()))
                .post("offenders/crn/X371505/referral/sent")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", equalTo(2503596167L),
                      "offenderCrn", equalTo("X371505"),
                      "type", equalTo("COAP"),
                      "provider", equalTo("N07"),
                      "team", equalTo("N07CHT"),
                      "staff", equalTo("N07A007"),                      
                      "officeLocation", equalTo("LDN_BCS"),
                      "date", equalTo("2021-06-02"),
                      "startTime", equalTo("10:00:00"),
                      "endTime", equalTo("12:00:00"),
                      "alert", equalTo(false),
                      "sensitive", equalTo(false),
                      "eventId", equalTo(2500428188L),   
                      "requirementId", equalTo(2500185174L));
    }

    private String createJwt(final String user, final List<String> roles) {
        return jwtAuthenticationHelper.createJwt(JwtParameters.builder()
                .username(user)
                .roles(roles)
                .scope(Arrays.asList("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }
}

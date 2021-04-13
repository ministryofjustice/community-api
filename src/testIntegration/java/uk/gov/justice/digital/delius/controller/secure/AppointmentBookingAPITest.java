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
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class AppointmentBookingAPITest extends IntegrationTestBase {

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
    public void shouldReturnOKAfterCreatingANewContact() {

        deliusApiMockServer.stubPostContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
                .when()
                .auth().oauth2(token)
                .contentType(String.valueOf(ContentType.APPLICATION_JSON))
                .body(writeValueAsString(AppointmentCreateRequest.builder()
                    .appointmentStart(OffsetDateTime.now())
                    .appointmentEnd(OffsetDateTime.now())
                    .officeLocationCode("CRSSHEF")
                    .notes("http://url")
                    .context("commissioned-rehabilitation-services")
                    .build()))
                .post("offenders/crn/X320741/sentence/2500295343/appointments")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .body("appointmentId", equalTo(2500029015L));
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

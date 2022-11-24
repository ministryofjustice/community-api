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
import uk.gov.justice.digital.delius.data.api.ContextlessNotificationCreateRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class NotificationsAPITest extends IntegrationTestBase {

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
    public void shouldReturnOKAfterCreatingANewContactUsingContextlessClientEndpoint() {

        deliusApiMockServer.stubPostContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(writeValueAsString(ContextlessNotificationCreateRequest.builder()
                .contractType("ACC")
                .referralStart(OffsetDateTime.of(2019, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC))
                .contactDateTime(OffsetDateTime.now())
                .notes("Contact notes plus link http://url")
                .build()))
            .post("offenders/crn/X320741/sentences/2500295345/notifications/context/commissioned-rehabilitation-services")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("contactId", equalTo(2500029015L));
    }

    @Test
    public void shouldReturnOKAfterRequestingANewContactThatAlreadyExists() {

        // No call to deliusApi required as contact already exists with Id 2592720451

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        OffsetDateTime contactDateTime = OffsetDateTime.of(2019, 9, 3, 2, 1, 3, 4, ZoneOffset.UTC);

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(writeValueAsString(ContextlessNotificationCreateRequest.builder()
                .contractType("ACC")
                .referralStart(OffsetDateTime.of(2019, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC))
                .contactDateTime(contactDateTime)
                .notes("Contact notes plus link http://url")
                .build()))
            .post("offenders/crn/X320741/sentences/2500295345/notifications/context/commissioned-rehabilitation-services")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("contactId", equalTo(2592720451L));
    }

    @Test
    public void shouldReturnBadRequestWhenNotFindingNsiUsingContextlessClientEndpoint() {

        deliusApiMockServer.stubPostContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_COMMUNITY_INTERVENTIONS_UPDATE"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body(writeValueAsString(ContextlessNotificationCreateRequest.builder()
                .contractType("ACC")
                .referralStart(OffsetDateTime.of(2019, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC))
                .contactDateTime(OffsetDateTime.now())
                .notes("Contact notes plus link http://url")
                .build()))
            .post("offenders/crn/X320741/sentences/99999999/notifications/context/commissioned-rehabilitation-services")
            .then()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("developerMessage", equalTo("Cannot find NSI for CRN: X320741 Sentence: 99999999 and ContractType ACC"));

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

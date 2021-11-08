package uk.gov.justice.digital.delius.controller.secure;

import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.specification.MultiPartSpecification;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.JwtAuthenticationHelper;
import uk.gov.justice.digital.delius.JwtParameters;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusApiExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusApiMockServer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class DeliusDocumentsAPITest extends IntegrationTestBase {

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
    @DisplayName("Will reject request without correct role")
    void willRejectRequestWithoutCorrectRole() {
        String crn = "X320741";
        String url = format("/offenders/%s/convictions/%s/document", crn, 2500029015L);

        final var token = createJwt("ROLE_COMMUNITY");
        given()
            .auth().oauth2(token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart(getFile())
            .when()
            .post(url)
            .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("Uploads a new UPW document to Delius")
    public void createNewDocumentInDelius() {
        deliusApiMockServer.stubPostContactToDeliusApi();
        deliusApiMockServer.stubPostNewDocumentToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_PROBATION"));
        String crn = "X320741";
        String url = format("/offenders/%s/convictions/%s/document", crn, 2500029015L);

        given()
            .when()
            .auth().oauth2(token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart(getFile())
            .post(url)
            .then()
            .assertThat()
            .statusCode(HttpStatus.CREATED.value())
            .body(
                "documentName", equalTo("upwDocument.pdf"),
                "crn", equalTo(crn)
            );
    }

    @Test
    @DisplayName("Returns Not Found when no Active Offender Manager is found for CRN")
    public void returnsNotFoundWhenNoActiveOffenderManagerForCrn() {
        deliusApiMockServer.stubPostContactToDeliusApi();

        final var token = createJwt("bob", Collections.singletonList("ROLE_PROBATION"));

        String crn = "X00000A";
        String url = format("/offenders/%s/convictions/%s/document", crn, 2500029015L);
        given()
            .when()
            .auth().oauth2(token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart(getFile())
            .post(url)
            .then()
            .assertThat()
            .statusCode(404);
    }

    private MultiPartSpecification getFile() {
        return new MultiPartSpecBuilder("Test-Content-In-File".getBytes()).
            fileName("upwDocument.pdf").
            controlName("file").
            mimeType("text/plain").
            build();
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

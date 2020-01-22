package uk.gov.justice.digital.delius.integration.secure;

import io.restassured.RestAssured;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.integration.wiremock.DeliusMockServer;
import uk.gov.justice.digital.delius.jwt.JwtAuthenticationHelper;
import uk.gov.justice.digital.delius.jwt.JwtAuthenticationHelper.JwtParameters;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseNoteAPITest {

    @LocalServerPort
    int port;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @ClassRule
    public static final DeliusMockServer deliusMockServer = new DeliusMockServer();

    @Before
    public void setup() {
        deliusMockServer.resetAll();
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
    }

    @Test
    public void shouldReturnOKWhenSendCaseNoteToDelius() {

        deliusMockServer.stubPutCaseNoteToDeliusCreated("54321", 12345L);

        final var token = createJwt("bob", Collections.singletonList("ROLE_DELIUS_CASE_NOTES"));

        final var response = given()
                .when()
                .auth().oauth2(token)
                .contentType(String.valueOf(ContentType.APPLICATION_JSON))
                .body("{\"content\":\"Bob\"}")
                .put("/nomisCaseNotes/54321/12345")
                .then()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .body()
                .asString();

        assertThat(response).isEqualTo(" XXXX (crn) had a Contact created.");

    }

    @Test
    public void shouldReturnNoContentWhenSendUpdateCaseNoteToDelius() {

        deliusMockServer.stubPutCaseNoteToDeliusNoContent("54321", 12345L);

        final var token = createJwt("bob", Collections.singletonList("ROLE_DELIUS_CASE_NOTES"));

        given()
                .when()
                .auth().oauth2(token)
                .contentType(String.valueOf(ContentType.APPLICATION_JSON))
                .body("{\"content\":\"Bob\"}")
                .put("/nomisCaseNotes/54321/12345")
                .then()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void shouldReturnBadRequestWhenSendCaseNoteToDelius() {

        deliusMockServer.stubPutCaseNoteToDeliusBadRequestError("54321", 12346L);

        final var token = createJwt("bob", Collections.singletonList("ROLE_DELIUS_CASE_NOTES"));

        given()
                .when()
                .auth().oauth2(token)
                .contentType(String.valueOf(ContentType.APPLICATION_JSON))
                .body("{\"content\":\"Bob\"}")
                .put("/nomisCaseNotes/54321/12346")
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());
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

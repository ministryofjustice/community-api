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
import uk.gov.justice.digital.delius.controller.wiremock.DeliusExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusMockServer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class CaseNoteAPITest {

    private static final DeliusMockServer deliusMockServer = new DeliusMockServer(8999);

    @RegisterExtension
    static DeliusExtension deliusExtension = new DeliusExtension(deliusMockServer);

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
    public void shouldReturnOKWhenSendCaseNoteToDelius() {

        deliusMockServer.stubPutCaseNoteToDeliusCreated("54321", 12345L);

        final var token = createJwt(Collections.singletonList("ROLE_DELIUS_CASE_NOTES"));

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

        deliusMockServer.stubPutCaseNoteToDeliusError("54321", 12345L, HttpStatus.NO_CONTENT);

        final var token = createJwt(Collections.singletonList("ROLE_DELIUS_CASE_NOTES"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body("{\"content\":\"Bob\"}")
            .put("/nomisCaseNotes/54321/12345")
            .then()
            .assertThat()
            .statusCode(204);
    }

    @Test
    public void shouldReturnBadRequestWhenSendCaseNoteToDelius() {

        deliusMockServer.stubPutCaseNoteToDeliusError("54321", 12346L, HttpStatus.BAD_REQUEST);

        final var token = createJwt(Collections.singletonList("ROLE_DELIUS_CASE_NOTES"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body("{\"content\":\"Bob\"}")
            .put("/nomisCaseNotes/54321/12346")
            .then()
            .assertThat()
            .statusCode(400);
    }

    @Test
    public void shouldReturnConflictWhenSendCaseNoteToDeliusServerErrorAndCaseNoteOMICOpdType() {

        deliusMockServer.stubPutCaseNoteToDeliusError("54321", 12346L, HttpStatus.INTERNAL_SERVER_ERROR);

        final var token = createJwt(Collections.singletonList("ROLE_DELIUS_CASE_NOTES"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body("{\"content\": \"Bob\", \"noteType\": \"OMIC_OPD Observation\", \"other\": \"field\"}")
            .put("/nomisCaseNotes/54321/12346")
            .then()
            .assertThat()
            .statusCode(409);
    }

    @Test
    public void shouldReturnConflictWhenSendCaseNoteToDeliusServerErrorAndCaseNoteOMICOpdTypeNoSpaces() {

        deliusMockServer.stubPutCaseNoteToDeliusError("54321", 12346L, HttpStatus.INTERNAL_SERVER_ERROR);

        final var token = createJwt(Collections.singletonList("ROLE_DELIUS_CASE_NOTES"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body("{\"content\":\"Bob\",\"noteType\":\"OMIC_OPD Observation\",\"other\":\"field\"}")
            .put("/nomisCaseNotes/54321/12346")
            .then()
            .assertThat()
            .statusCode(409);
    }

    @Test
    public void shouldReturnServerErrorWhenSendCaseNoteToDeliusServerError() {

        deliusMockServer.stubPutCaseNoteToDeliusError("54321", 12346L, HttpStatus.INTERNAL_SERVER_ERROR);

        final var token = createJwt(Collections.singletonList("ROLE_DELIUS_CASE_NOTES"));

        given()
            .when()
            .auth().oauth2(token)
            .contentType(String.valueOf(ContentType.APPLICATION_JSON))
            .body("{\"content\":\"Bob\"}")
            .put("/nomisCaseNotes/54321/12346")
            .then()
            .assertThat()
            .statusCode(500);
    }


    private String createJwt(final List<String> roles) {
        return jwtAuthenticationHelper.createJwt(JwtParameters.builder()
            .username("bob")
            .roles(roles)
            .scope(Arrays.asList("read", "write"))
            .expiryTime(Duration.ofDays(1))
            .build());
    }
}

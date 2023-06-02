package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.AuthPassword;
import uk.gov.justice.digital.delius.data.api.AuthUser;

import static io.restassured.RestAssured.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AuthenticationAPITest extends IntegrationTestBase {

    @Test
    public void notValid400ReturnedWhenPasswordEmpty() {

        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
                .contentType(APPLICATION_JSON_VALUE)
                .body(AuthPassword.builder().build())
                .when()
                .post("/users/oliver.connolly/password")
                .then()
                .statusCode(400);
    }

    @Test
    public void notValid400ReturnedWhenBodyEmpty() {

        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .post("/users/oliver.connolly/password")
                .then()
                .statusCode(400);
    }
}

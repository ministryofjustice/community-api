package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.AuthPassword;
import uk.gov.justice.digital.delius.data.api.AuthUser;

import static io.restassured.RestAssured.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AuthenticationAPITest extends IntegrationTestBase {

    @Test
    public void authenticateReturnsOKWhenUsernamePasswordMatch() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthUser.builder().username("oliver.connolly").password("secret").build())
            .when()
            .post("/authenticate")
            .then()
            .statusCode(200);
    }

    @Test
    public void authenticateReturns400WhenNoPassword() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthUser.builder().username("oliver.connolly").build())
            .when()
            .post("/authenticate")
            .then()
            .statusCode(400);
    }

    @Test
    public void authenticateReturns400WhenNoUsername() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthUser.builder().password("secret").build())
            .when()
            .post("/authenticate")
            .then()
            .statusCode(400);
    }

    @Test
    public void authenticateReturns400WhenBodyEmpty() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/authenticate")
            .then()
            .statusCode(400);
    }

    @Test
    public void authenticateReturnsUNAUTHORIZEDWhenUsernamePasswordDoNotMatch() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthUser.builder().username("oliver.connolly").password("incorrectpassword").build())
            .when()
            .post("/authenticate")
            .then()
            .statusCode(401);
    }

    @Test
    public void authenticateReturnsUNAUTHORIZEDWhenUsernameNotFound() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthUser.builder().username("not.exists").password("secret").build())
            .when()
            .post("/authenticate")
            .then()
            .statusCode(401);
    }

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

    @Test
    public void canChangeAUsersPassword() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthUser.builder().username("oliver.connolly").password("secret").build())
            .when()
            .post("/authenticate")
            .then()
            .statusCode(200);

        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthPassword.builder().password("newsecret").build())
            .when()
            .post("/users/oliver.connolly/password")
            .then()
            .statusCode(200);

        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthUser.builder().username("oliver.connolly").password("secret").build())
            .when()
            .post("/authenticate")
            .then()
            .statusCode(401);

        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthUser.builder().username("oliver.connolly").password("newsecret").build())
            .when()
            .post("/authenticate")
            .then()
            .statusCode(200);

        // restore to original password
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType(APPLICATION_JSON_VALUE)
            .body(AuthPassword.builder().password("secret").build())
            .when()
            .post("/users/oliver.connolly/password")
            .then()
            .statusCode(200);
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.data.api.UserRole;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class UserAPITest extends IntegrationTestBase {
    @Test
    public void usersDetails_success_role_community_users() {
        final var userDetails = given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_USERS"))
            .contentType("text/plain")
            .when()
            .get("/users/bernard.beaks/details")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(UserDetails.class);

        assertThat(userDetails.getFirstName()).isEqualTo("Bernard");
        assertThat(userDetails.getSurname()).isEqualTo("Beaks");
        assertThat(userDetails.getEmail()).isEqualTo("bernard.beaks@justice.gov.uk");
        assertThat(userDetails.getRoles()).hasSize(1).contains(UserRole.builder().name("UWBT060").build());
    }

    @Test
    public void usersDetails_success_role_community_auth_int() {
        final var userDetails = given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_AUTH_INT"))
            .contentType("text/plain")
            .when()
            .get("/users/bernard.beaks/details")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(UserDetails.class);

        assertThat(userDetails.getFirstName()).isEqualTo("Bernard");
        assertThat(userDetails.getSurname()).isEqualTo("Beaks");
        assertThat(userDetails.getEmail()).isEqualTo("bernard.beaks@justice.gov.uk");
        assertThat(userDetails.getRoles()).hasSize(1).contains(UserRole.builder().name("UWBT060").build());
    }

    @Test
    public void usersDetails_success_role_community_users_roles() {
        final var userDetails = given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_USERS_ROLES"))
            .contentType("text/plain")
            .when()
            .get("/users/bernard.beaks/details")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(UserDetails.class);

        assertThat(userDetails.getFirstName()).isEqualTo("Bernard");
        assertThat(userDetails.getSurname()).isEqualTo("Beaks");
        assertThat(userDetails.getEmail()).isEqualTo("bernard.beaks@justice.gov.uk");
        assertThat(userDetails.getRoles()).hasSize(1).contains(UserRole.builder().name("UWBT060").build());
    }

    @Test
    public void usersDetails_returns404WhenUserNotFound() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_USERS"))
            .contentType("text/plain")
            .when()
            .get("/users/john.smith/details")
            .then()
            .statusCode(404);
    }

    @Test
    public void usersDetails_returns403WhenForbiddenByRole() {
        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY"))
            .contentType("text/plain")
            .when()
            .get("/users/john.smith/details")
            .then()
            .statusCode(403);
    }

    @Test
    @DirtiesContext
    public void addRole() {

        given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_USERS_ROLES"))
            .contentType("text/plain")
            .when()
            .put("/users/bernard.beaks/roles/CWBT001")
            .then()
            .statusCode(200);


        final var userDetails = given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_USERS_ROLES"))
            .contentType("text/plain")
            .when()
            .get("/users/bernard.beaks/details")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(UserDetails.class);

        assertThat(userDetails.getFirstName()).isEqualTo("Bernard");
        assertThat(userDetails.getSurname()).isEqualTo("Beaks");
        assertThat(userDetails.getEmail()).isEqualTo("bernard.beaks@justice.gov.uk");
        assertThat(userDetails.getRoles()).hasSize(2).contains(
            UserRole.builder().name("CWBT001").build(),
            UserRole.builder().name("UWBT060").build());
    }

    @Test
    public void addRole_whenUserDoesNotExist() {

        final var response = given()
            .auth()
            .oauth2(createJwt("ROLE_COMMUNITY_USERS_ROLES"))
            .contentType("text/plain")
            .when()
            .put("/users/usernoexist/roles/CWBT001")
            .then()
            .statusCode(404)
            .extract()
            .as(ErrorResponse.class);

        assertThat(response).isEqualTo(ErrorResponse.builder()
            .status(404)
            .developerMessage("Could not find user with username: 'usernoexist'")
            .build());
    }
}

package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.UserDetails;
import uk.gov.justice.digital.delius.data.api.UserRole;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "delius.ldap.users.base=ou=people,dc=memorynotfound,dc=com",
        "features.auth.experimental=true"
})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class AuthenticationAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
    }

    @Test
    public void authenticateReturnsOKWhenUsernamePasswordMatch() {
        given()
                .contentType("text/plain")
                .param("username", "oliver.connolly")
                .param("password", "secret")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(200);
    }

    @Test
    public void authenticateReturnsUNAUTHORIZEDWhenUsernamePasswordDoNotMatch() {
        given()
                .contentType("text/plain")
                .param("username", "oliver.connolly")
                .param("password", "incorrectpassword")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(401);
    }

    @Test
    public void authenticateReturnsUNAUTHORIZEDWhenUsernameNotFound() {
        given()
                .contentType("text/plain")
                .param("username", "not.exists")
                .param("password", "secret")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(401);
    }

    @Test
    public void canChangeAUsersPassword() {
        given()
                .contentType("text/plain")
                .param("username", "oliver.connolly")
                .param("password", "secret")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(200);

        given()
                //.contentType("multipart/form-data")
                .formParam("password", "newsecret")
                .when()
                .post("/users/oliver.connolly/password")
                .then()
                .statusCode(200);

        given()
                .contentType("text/plain")
                .param("username", "oliver.connolly")
                .param("password", "secret")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(401);

        given()
                .contentType("text/plain")
                .param("username", "oliver.connolly")
                .param("password", "newsecret")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(200);

        // restore to original password
        given()
                //.contentType("multipart/form-data")
                .formParam("password", "secret")
                .when()
                .post("/users/oliver.connolly/password")
                .then()
                .statusCode(200);
    }
    @Test
    public void canLockUsersAccount() {
        given()
                .contentType("text/plain")
                .param("username", "oliver.connolly")
                .param("password", "secret")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(200);

        given()
                //.contentType("multipart/form-data")
                .when()
                .post("/users/oliver.connolly/lock")
                .then()
                .statusCode(200);

        given()
                .contentType("text/plain")
                .param("username", "oliver.connolly")
                .param("password", "secret")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(401);

    }
    @Test
    public void canUnlockUsersAccount() {
        given()
                //.contentType("multipart/form-data")
                .when()
                .post("/users/oliver.connolly/lock")
                .then()
                .statusCode(200);

        given()
                .contentType("text/plain")
                .param("username", "oliver.connolly")
                .param("password", "secret")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(401);

        given()
                //.contentType("multipart/form-data")
                .when()
                .post("/users/oliver.connolly/unlock")
                .then()
                .statusCode(200);


        given()
                .contentType("text/plain")
                .param("username", "oliver.connolly")
                .param("password", "secret")
                .when()
                .get("/authenticate")
                .then()
                .statusCode(200);


    }

    @Test
    public void returnsUserDetails() {
        final UserDetails userDetails = given()
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
        assertThat(userDetails.getRoles()).hasSize(1).contains(UserRole.builder().name("UWBT060").description("UPW Admin (national)").build());
    }

}
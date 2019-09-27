package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.wrapper.UserRepositoryWrapper;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class LogonAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    @MockBean
    private UserRepositoryWrapper userRepositoryWrapper;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));

        when(userRepositoryWrapper.getUser(anyString())).thenReturn(aUser());
    }

    private User aUser() {
        return User.builder().userId(1l).distinguishedName("oliver.connolly").build();
    }

    @Test
    public void logonWithMissingBodyGivesBadRequest() {
        given()
                .contentType("text/plain")
                .when()
                .post("/logon")
                .then()
                .statusCode(400);
    }

    @Test
    public void logonWithInvalidBodyGivesBadRequest() {
        given()
                .body("some old nonsense")
                .when()
                .post("/logon")
                .then()
                .statusCode(400);
    }

    @Test
    public void logonWithUnknownButOtherwiseValidDistinguishedNameGivesNotFound() {
        given()
                .body("uid=jimmysnozzle,ou=people,dc=memorynotfound,dc=com")
                .when()
                .post("/logon")
                .then()
                .statusCode(404);
    }

    @Test
    public void logonWithKnownDistinguishedNameGivesTokenContainingOracleUser() {
        String token = given()
                .body("uid=jihn,ou=people,dc=memorynotfound,dc=com")
                .when()
                .post("/logon")
                .then()
                .statusCode(200)
                .extract().body().asString();

        assertThat(jwt.parseToken(token).get().get(Jwt.UID)).isEqualTo("Jihndie1");
    }

    @Test
    public void logonWithNationalUserDistinguishedNameGivesTokenContainingNationalUser() {
        String token = given()
                .body("NationalUser")
                .when()
                .post("/logon")
                .then()
                .statusCode(200)
                .extract().body().asString();

        assertThat(jwt.parseToken(token).get().get(Jwt.UID)).isEqualTo("NationalUser");
    }

}

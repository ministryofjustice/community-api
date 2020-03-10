package uk.gov.justice.digital.delius.integration.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.UserDetails;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
public class UserAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.auth}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }


    @Test
    public void retrieveUserDetailsForMultipleUsers() throws JsonProcessingException {
        var userDetails = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .body(objectMapper.writeValueAsString(Set.of("sheilahancocknps", "JimSnowLdap")))
                .when()
                .post("users/list/detail")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(UserDetails[].class);

        UserDetails jimSnowUserDetails = Stream.of(userDetails).filter(u -> u.getUsername().equals("JimSnowLdap")).findFirst().orElseThrow();
        UserDetails sheilaHancockUserDetails = Stream.of(userDetails).filter(u -> u.getUsername().equals("sheilahancocknps")).findFirst().orElseThrow();

        assertThat(userDetails.length).isEqualTo(2);

        assertThat(jimSnowUserDetails).isNotNull();
        assertThat(jimSnowUserDetails.getEmail()).isEqualTo("jim.snow@justice.gov.uk");
        assertThat(jimSnowUserDetails.getFirstName()).isEqualTo("Jim");
        assertThat(jimSnowUserDetails.getSurname()).isEqualTo("Snow");

        assertThat(sheilaHancockUserDetails).isNotNull();
        assertThat(sheilaHancockUserDetails.getEmail()).isEqualTo("sheila.hancock@justice.gov.uk");
        assertThat(sheilaHancockUserDetails.getFirstName()).isEqualTo("Sheila");
        assertThat(sheilaHancockUserDetails.getSurname()).isEqualTo("Hancock");
    }

    @Test
    public void retrieveDetailsWhenUsersDoNotExist() throws JsonProcessingException {
        val userDetails = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .body(objectMapper.writeValueAsString(Set.of("xxxyyyzzzuser", "aaabbbcccuser")))
                .when()
                .post("users/list/detail")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(UserDetails[].class);

        assertThat(userDetails).isEmpty();
    }

    @Test
    public void retrieveDetailsWhenUsersExistAndDoNotExist() throws JsonProcessingException {
        val userDetails = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .body(objectMapper.writeValueAsString(Set.of("xxxyyyzzzuser", "JimSnowLdap")))
                .when()
                .post("users/list/detail")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(UserDetails[].class);

        UserDetails jimSnowUserDetails = Stream.of(userDetails).filter(u -> u.getUsername().equals("JimSnowLdap")).findFirst().orElseThrow();

        assertThat(userDetails.length).isEqualTo(1);

        assertThat(jimSnowUserDetails).isNotNull();
        assertThat(jimSnowUserDetails.getEmail()).isEqualTo("jim.snow@justice.gov.uk");
        assertThat(jimSnowUserDetails.getFirstName()).isEqualTo("Jim");
        assertThat(jimSnowUserDetails.getSurname()).isEqualTo("Snow");
    }

    @Test
    public void retrieveMultipleUserDetailsWithNoBodyContentReturn400() {
            given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .post("users/list/detail")
                .then()
                .statusCode(400);
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.UserDetailsWrapper;

import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class UserAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.auth}")
    private String validOauthToken;

    @BeforeEach
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
                .as(JsonNode.class)
                .toString();

        String expectedJson = "{" +
                        "\"userDetailsList\":[{" +
                           "\"firstName\":\"Jim\"," +
                           "\"surname\":\"Snow\"," +
                           "\"email\":\"jim.snow@justice.gov.uk\"," +
                           "\"enabled\":true," +
                           "\"username\":\"JimSnowLdap\"" +
                       "},{" +
                           "\"firstName\":\"Sheila\"," +
                           "\"surname\":\"Hancock\"," +
                           "\"email\":\"sheila.hancock@justice.gov.uk\"," +
                           "\"enabled\":true," +
                           "\"username\":\"sheilahancocknps\"" +
                       "}]" +
                       "}";

        assertThat(userDetails).isEqualTo(expectedJson);
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
                .as(UserDetailsWrapper.class)
                .getUserDetailsList();

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
                .as(JsonNode.class)
                .toString();

        String expectedJson = "{" +
                "\"userDetailsList\":[{" +
                "\"firstName\":\"Jim\"," +
                "\"surname\":\"Snow\"," +
                "\"email\":\"jim.snow@justice.gov.uk\"," +
                "\"enabled\":true," +
                "\"username\":\"JimSnowLdap\"" +
                "}]" +
                "}";

        assertThat(userDetails).isEqualTo(expectedJson);
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

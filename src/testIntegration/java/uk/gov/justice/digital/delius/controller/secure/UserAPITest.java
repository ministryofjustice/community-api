package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.UserDetailsWrapper;

import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class UserAPITest extends IntegrationTestBase {
    @Test
    public void retrieveUserDetailsForMultipleUsers() {
        var userDetails = given()
                .auth()
                .oauth2(createJwt("ROLE_AUTH_DELIUS_LDAP"))
                .contentType(APPLICATION_JSON_VALUE)
                .body(writeValueAsString(Set.of("sheilahancocknps", "JimSnowLdap")))
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
    public void retrieveDetailsWhenUsersDoNotExist() {
        val userDetails = given()
                .auth()
                .oauth2(createJwt("ROLE_AUTH_DELIUS_LDAP"))
                .contentType(APPLICATION_JSON_VALUE)
                .body(writeValueAsString(Set.of("xxxyyyzzzuser", "aaabbbcccuser")))
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
    public void retrieveDetailsWhenUsersExistAndDoNotExist() {
        val userDetails = given()
                .auth()
                .oauth2(createJwt("ROLE_AUTH_DELIUS_LDAP"))
                .contentType(APPLICATION_JSON_VALUE)
                .body(writeValueAsString(Set.of("xxxyyyzzzuser", "JimSnowLdap")))
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
                .oauth2(createJwt("ROLE_AUTH_DELIUS_LDAP"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .post("users/list/detail")
                .then()
                .statusCode(400);
    }
}

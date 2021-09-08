package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class StaffResource_CasesAPITest extends IntegrationTestBase {
    @Test
    public void getCasesForUser() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("staff/username/bernard.beaks/cases")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", equalTo(1))
            .body("size", equalTo(10000))
            .body("numberOfElements", equalTo(1))
            .body("content.size()", equalTo(1))
            .root("content.find { it.crn == '%s' }")

            .body("firstName", withArgs("X330899"), equalTo("Bryn"))
            .body("middleNames", withArgs("X330899"), equalTo(List.of("Edam")))
            .body("surname", withArgs("X330899"), equalTo("Brown"));
    }
}

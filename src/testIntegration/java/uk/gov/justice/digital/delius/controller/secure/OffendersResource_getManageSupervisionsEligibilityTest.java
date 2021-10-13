package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getManageSupervisionsEligibilityTest extends IntegrationTestBase {

    @Test
    public void getsEligibleOffender() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X330899/manage-supervisions-eligibility")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("firstName", equalTo("Bryn"))
            .body("middleNames", equalTo(List.of("Edam")))
            .body("surname", equalTo("Brown"));
    }

    @Test
    public void failsToGetIneligibleOffender() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/manage-supervisions-eligibility")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}

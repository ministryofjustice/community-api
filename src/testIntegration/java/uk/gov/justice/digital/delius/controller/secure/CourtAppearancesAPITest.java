package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasicWrapper;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CourtAppearancesAPITest extends IntegrationTestBase {

    @Test
    public void mustHaveCommunityRole() {
        final var token = createJwt("ROLE_BANANAS");

        given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/courtAppearances")
                .then()
                .statusCode(403);
    }

    @Test
    public void canGetCourtAppearances() {
        var result = given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .param("fromDate","2019-09-04")
                .when()
                .get("/courtAppearances")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtAppearanceBasicWrapper.class);
        assertThat(result.getCourtAppearances()).extracting("courtAppearanceId").containsExactly(2500316926L, 2500319107L);
    }

    @Test
    public void canGetCourtAppearancesNoResults() {
        var result = given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .param("fromDate","2034-09-05")
                .when()
                .get("/courtAppearances")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtAppearanceBasicWrapper.class);
        assertThat(result.getCourtAppearances()).hasSize(0);
    }
}

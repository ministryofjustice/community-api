package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;

import uk.gov.justice.digital.delius.data.api.OffenderDetail;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_updateTier extends IntegrationTestBase {

    @Test
    public void updatesTier() {
        final var originalOffender = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/all")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(OffenderDetail.class);

        assertThat(originalOffender.getCurrentTier()).isEqualTo("D2");
        final var updatedTierOffender = given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/B1")
            .then()
            .statusCode(200).extract()
            .body()
            .as(OffenderDetail.class);

        assertThat(updatedTierOffender.getCurrentTier()).isEqualTo("B1");

        final var updatedOffender = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/all")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(OffenderDetail.class);

        assertThat(updatedOffender.getCurrentTier()).isEqualTo("B1");
    }


    @Test
    public void updatesTier_offenderNotFound_returnsNotFound() {
        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/XNOTFOUND/tier/B1")
            .then()
            .statusCode(404);
    }

    @Test
    public void updatesTier_wrongRole_returnsForbidden() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/B1")
            .then()
            .statusCode(403);
    }

    @Test
    public void updatesTier_tierNotFound_returns404() {
        given()
            .auth()
            .oauth2(tokenWithRoleManagementTierUpdate())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .post("/offenders/crn/X320741/tier/NOTFOUND")
            .then()
            .statusCode(404);
    }

}

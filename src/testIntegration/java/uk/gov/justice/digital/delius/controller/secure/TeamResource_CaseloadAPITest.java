package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.ManagedEventId;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class TeamResource_CaseloadAPITest extends IntegrationTestBase {
    @Test
    public void getCaseloadByTeamCode() {
        var caseload = given()
            .auth().oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("team/C01T04/caseload")
            .then()
            .extract().body().as(Caseload.class);

        assertThat(caseload.getManagedOffenders())
            .hasSize(3)
            .extracting(ManagedOffenderCrn::getStaffIdentifier).contains(11L, 11L, 11L);
        assertThat(caseload.getSupervisedOrders())
            .hasSize(2)
            .extracting(ManagedEventId::getStaffIdentifier).contains(11L, 13L);
    }

    @Test
    public void managedOffendersOnly() {
        var caseload = given()
            .auth().oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("team/C01T04/caseload/managedOffenders")
            .then()
            .extract().body().as(ManagedOffenderCrn[].class);

        assertThat(caseload)
            .hasSize(3)
            .extracting(ManagedOffenderCrn::getStaffIdentifier).contains(11L, 11L, 11L);
    }

    @Test
    public void supervisedOrdersOnly() {
        var caseload = given()
            .auth().oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("team/C01T04/caseload/supervisedOrders")
            .then()
            .extract().body().as(ManagedEventId[].class);

        assertThat(caseload)
            .hasSize(2)
            .extracting(ManagedEventId::getStaffIdentifier).contains(11L, 13L);
    }

    @Test
    public void emptyCaseload() {
        var caseload = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("team/C01T05/caseload")
            .then()
            .statusCode(200)
            .extract().body().as(Caseload.class);
        assertThat(caseload.getManagedOffenders()).isNullOrEmpty();
        assertThat(caseload.getSupervisedOrders()).isNullOrEmpty();
    }

    @Test
    public void missingTeam() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("team/999999/caseload")
            .then()
            .statusCode(404);
    }

    @Test
    public void invalidTeamCode() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("team/INVALID123/caseload")
            .then()
            .statusCode(400)
            .body("'getCaseloadForTeam.teamCode'", contains("must be 6 alphanumeric characters"));
    }
}
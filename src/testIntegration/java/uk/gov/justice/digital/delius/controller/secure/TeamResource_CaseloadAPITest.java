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
}

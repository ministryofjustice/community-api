package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class StaffResource_ManagedOffendersAPITest extends IntegrationTestBase {
    @Test
    public void getCurrentManagedOffendersForOfficer() {
        ManagedOffender[] managedOffenders = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffIdentifier/11/managedOffenders?current=true")
                .then()
                .extract()
                .body()
                .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).hasSize(3);
        assertThat(mos).extracting("staffIdentifier").contains(11L, 11L, 11L);
    }

    @Test
    public void getAllManagedOffendersForOfficer() {
        ManagedOffender[] managedOffenders = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffIdentifier/11/managedOffenders")
                .then()
                .extract()
                .body()
                .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).hasSize(3);
    }

    @Test
    public void getInvalidOfficerNotFound() {

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/staffIdentifier/9999/managedOffenders")
                .then()
                .statusCode(404);
    }

    @Test
    public void getManagedOffendersSoftDeletedCheck() {

        /*
         This officer has two offenders assigned but one is SOFT_DELETED in seed data.
         The response should be only one managed offender - G3232VA.
        */

        ManagedOffender[] managedOffenders =
                given()
                        .auth()
                        .oauth2(tokenWithRoleCommunity())
                        .contentType(APPLICATION_JSON_VALUE)
                        .when()
                        .get("/staff/staffIdentifier/18/managedOffenders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).hasSize(1);
        assertThat(mos.get(0).getNomsNumber()).isEqualToIgnoringCase("G3232VA");
    }

    @Test
    public void getUnassignedOfficerEmptyList() {

        ManagedOffender[] managedOffenders =
                given()
                        .auth()
                        .oauth2(tokenWithRoleCommunity())
                        .contentType(APPLICATION_JSON_VALUE)
                        .when()
                        .get("/staff/staffIdentifier/16/managedOffenders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .as(ManagedOffender[].class);

        List<ManagedOffender> mos = Arrays.asList(managedOffenders);
        assertThat(mos).isEmpty();
    }


}

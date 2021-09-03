package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class StaffResource_CasesAPITest extends IntegrationTestBase {
    @Test
    public void getCurrentManagedOffendersForOfficer() {
        OffenderDetailSummary[] offenders = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/username/bernard.beaks/cases")
                .then()
                .extract()
                .body()
                .as(OffenderDetailSummary[].class);

        List<OffenderDetailSummary> mos = Arrays.asList(offenders);
        assertThat(mos).hasSize(1);
        assertThat(mos).extracting("offenderId").contains(2500343964L);
    }
}

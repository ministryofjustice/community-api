package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Case;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class StaffResource_CasesAPITest extends IntegrationTestBase {
    @Test
    public void getCasesForUser() {
        Case[] cases = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("staff/username/bernard.beaks/cases")
                .then()
                .extract()
                .body()
                .as(Case[].class);

        List<Case> mos = Arrays.asList(cases);
        assertThat(mos).hasSize(1);
        assertThat(mos).extracting("crn").contains("X320741");
        assertThat(mos).extracting("firstName").contains("Aadland");
        assertThat(mos).extracting("middleNames").contains(List.of("Danger"));
        assertThat(mos).extracting("surname").contains("Bertrand");
    }
}

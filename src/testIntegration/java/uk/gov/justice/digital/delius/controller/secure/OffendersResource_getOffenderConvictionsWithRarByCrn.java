package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Conviction;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getOffenderConvictionsWithRarByCrn extends IntegrationTestBase {

    @Test
    public void getOffenderConvictionsWithRarByCrn() {

        final var convictions = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/convictions-with-rar")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Conviction[].class);

        assertThat(convictions).extracting("convictionId").containsExactly(2500295345L);
        assertThat(convictions[0].getReferralDate()).isEqualTo(LocalDate.of(2018, 9, 4));
    }

    @Test
    public void getOffenderConvictionsByCrn_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X777777/convictions-with-rar")
                .then()
                .statusCode(404);
    }
}

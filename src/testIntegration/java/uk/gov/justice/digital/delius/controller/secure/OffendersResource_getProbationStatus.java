package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.ProbationStatus;
import uk.gov.justice.digital.delius.data.api.ProbationStatusDetail;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getProbationStatus extends IntegrationTestBase {

    @Test
    public void canGetOffenderProbationStatusByCrn() {
        final var offenderDetail = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X320741/probationStatus")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(ProbationStatusDetail.class);

        assertThat(offenderDetail).isNotNull();
        assertThat(offenderDetail.getProbationStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(offenderDetail.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2019, 9, 17));
        assertThat(offenderDetail.getInBreach()).isEqualTo(false);
        assertThat(offenderDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void return404IfOffenderDoesNotExist() {
        final var response = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/UNKNOWN/probationStatus")
            .then()
            .statusCode(404)
            .extract()
            .body()
            .as(ErrorResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getDeveloperMessage()).isEqualTo("Offender not found");
    }
}

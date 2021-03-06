package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.OffenderAssessments;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getAssessmentsByCrn extends IntegrationTestBase {

    @Test
    public void canGetOffenderAssessmentsByCrn() {
        final var offenderAssessments = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/assessments")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderAssessments.class);

      OffenderAssessments expectedAssessments = OffenderAssessments.builder().rsrScore(1D).ogrsScore(27).build();
      assertThat(offenderAssessments).isEqualTo(expectedAssessments);
    }

    @Test
    public void noOGRSAssessmentSoUsesOasysAssessment() {
        final var offenderAssessments = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320811/assessments")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderAssessments.class);

      OffenderAssessments expectedAssessments = OffenderAssessments.builder().rsrScore(1D).ogrsScore(2).build();
      assertThat(offenderAssessments).isEqualTo(expectedAssessments);
    }
    
    @Test
    public void canGetOffenderAssessmentsByCrn_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X777777/assessments")
                .then()
                .statusCode(404);
    }

}

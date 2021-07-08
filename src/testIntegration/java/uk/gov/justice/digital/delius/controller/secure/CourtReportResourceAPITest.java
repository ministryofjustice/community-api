package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class CourtReportResourceAPITest extends IntegrationTestBase {

    private static final String COURT_REPORT_PATH_FORMAT = "/offenders/crn/%s/courtReports/%s";

    @Test
    void givenUnknownCrn_whenGetCourtReport_thenNotFound() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(COURT_REPORT_PATH_FORMAT, "X9999999", "123"))
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void givenKnownCrnUnknownReportId_whenGetCourtReport_thenNotFound() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(COURT_REPORT_PATH_FORMAT, "X320741", "123"))
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void givenKnownValues_whenGetCourtReport_thenReturn() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(COURT_REPORT_PATH_FORMAT, "X320741", "1"))
            .then()
            .statusCode(200)
            .body("offenderId", is(2500343964L))
            .body("courtReportId", is(1))
            .body("requestedDate", equalTo("2021-02-01T00:00:00"))
            .body("requiredDate", equalTo("2021-02-08T00:00:00"))
            .body("allocationDate", equalTo("2021-02-02T00:00:00"))
            .body("completedDate", equalTo("2021-02-04T00:00:00"))
            .body("sentToCourtDate", equalTo("2021-02-04T00:00:00"))
            .body("receivedByCourtDate", equalTo("2021-02-05T00:00:00"))
            .body("courtReportType.code", equalTo("CJF"))
            .body("courtReportType.description", equalTo("Pre-Sentence Report - Fast"));
    }

    @Test
    void givenKnownValuesButSoftDeletedReport_whenGetCourtReport_thenNotFound() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(COURT_REPORT_PATH_FORMAT, "X320741", "2"))
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}

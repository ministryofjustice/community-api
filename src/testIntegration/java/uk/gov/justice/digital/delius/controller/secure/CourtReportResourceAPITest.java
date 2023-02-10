package uk.gov.justice.digital.delius.controller.secure;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static java.time.Month.SEPTEMBER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class CourtReportResourceAPITest extends IntegrationTestBase {

    private static final String COURT_REPORT_PATH_FORMAT = "/offenders/crn/%s/courtReports/%s";
    private static final String REPORTS_BY_CONVICTION_ID_PATH_FORMAT = "/offenders/crn/%s/convictions/%s/courtReports";

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

    @Test
    void givenKnownValues_whenGetCourtReportsByCrnAndConvictionId_thenReturn() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(REPORTS_BY_CONVICTION_ID_PATH_FORMAT, "X320741", "2500295345"))
            .then()
            .statusCode(200)
            .body("$.size()", Matchers.is(2))
            .body("[0].courtReportId", equalTo(2500079873L))
            .body("[0].offenderId", equalTo(2500343964L))
            .body("[0].requestedDate", equalTo(standardDateTimeOf(2018, SEPTEMBER, 4)))
            .body("[0].requiredDate", equalTo(standardDateTimeOf(2019, SEPTEMBER, 4)))
            .body("[0].courtReportType.code", equalTo("CJF"))
            .body("[0].courtReportType.description", equalTo("Pre-Sentence Report - Fast"))
            .body("[0].deliveredCourtReportType.code", equalTo("NIL"))
            .body("[0].deliveredCourtReportType.description", equalTo("Abandoned/Nil Report"))
            .body("[0].reportManagers", hasSize(2))
            .body("[0].reportManagers[0].active", is(true))
            .body("[0].reportManagers[0].staff.forenames", equalTo("Unallocated Staff(N02)"))
            .body("[0].reportManagers[0].staff.surname", equalTo("Staff"))
            .body("[0].reportManagers[0].staff.unallocated", is(true))
            .body("[1].reportManagers", hasSize(1))
            .body("[1].courtReportType.code", equalTo("ADR"))
            .body("[1].courtReportType.description", equalTo("Assessment - DRR"))
            .body("[1].deliveredCourtReportType.code", equalTo("PSR"))
            .body("[1].deliveredCourtReportType.description", equalTo("Pre-Sentence Report"))
        ;
    }

    @Test
    void givenUnknownCrn_whenGetCourtReports_thenNotFound() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(REPORTS_BY_CONVICTION_ID_PATH_FORMAT, "X9999999", "2500295345"))
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void givenKnownCrnUnknownConvictionId_whenGetCourtReports_thenNotFound() {
        given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(REPORTS_BY_CONVICTION_ID_PATH_FORMAT, "X320741", "123"))
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    public static String standardDateTimeOf(int year, Month month, int dayOfMonth) {
        return LocalDateTime.of(year, month, dayOfMonth, 0, 0, 0).format(DateTimeFormatter.ISO_DATE_TIME);
    }
}

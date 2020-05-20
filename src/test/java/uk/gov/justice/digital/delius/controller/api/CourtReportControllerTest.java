package uk.gov.justice.digital.delius.controller.api;

import com.google.common.collect.ImmutableList;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.service.CourtReportService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtReportControllerTest {

    @Mock
    private OffenderService offenderService;

    @Mock
    private CourtReportService courtReportService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new CourtReportController(offenderService, courtReportService)
        );
    }

    @Test
    public void canGetAllReportsForOffenderByCrn() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(courtReportService.courtReportsFor(any())).thenReturn(ImmutableList.of(
                CourtReport.builder().courtReportId(1L).offenderId(1L).dateRequested(LocalDateTime.now()).build(),
                CourtReport.builder().courtReportId(2L).offenderId(1L).dateRequested(LocalDateTime.now()).build(),
                CourtReport.builder().courtReportId(4L).offenderId(1L).dateRequested(LocalDateTime.now()).build()
        ));

        CourtReport[] courtReports = given()
            .when()
            .get("/api/offenders/crn/CRN1/courtReports")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtReport[].class);

        assertThat(courtReports).hasSize(3);
    }

    @Test
    public void missingOffenderRecordResultsInAllReportsForOffenderByCrnNotFound() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/crn/CRN1/courtReports")
            .then()
            .statusCode(404);

    }

    @Test
    public void canGetSpecificReportForOffenderByCrnAndReportId() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(courtReportService.courtReportFor(any(), any())).thenReturn(
                Optional.of(CourtReport.builder().courtReportId(4L).offenderId(1L).build()));

        CourtReport courtReport = given()
            .when()
            .get("/api/offenders/crn/CRN1/courtReports/4")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtReport.class);

        assertThat(courtReport).isNotNull();
        assertThat(courtReport.getCourtReportId()).isEqualTo(4);
    }

    @Test
    public void missingOffenderRecordResultsInReportForOffenderByCrnAndReportIdNotFound() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.empty());

        given()
                .when()
                .get("/api/offenders/crn/CRN1/courtReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void missingCourtReportRecordResultsInReportForOffenderByCrnAndReportIdNotFound() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(courtReportService.courtReportFor(any(), any())).thenReturn(Optional.empty());

        given()
                .when()
                .get("/api/offenders/crn/CRN1/courtReports/4")
                .then()
                .statusCode(404);
    }


    @Test
    public void canGetAllReportsForOffenderByNomsNumber() {
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.of(1L));
        when(courtReportService.courtReportsFor(any())).thenReturn(ImmutableList.of(
                CourtReport.builder().courtReportId(1L).offenderId(1L).dateRequested(LocalDateTime.now()).build(),
                CourtReport.builder().courtReportId(2L).offenderId(1L).dateRequested(LocalDateTime.now()).build(),
                CourtReport.builder().courtReportId(4L).offenderId(1L).dateRequested(LocalDateTime.now()).build()
        ));

        CourtReport[] courtReports = given()
            .when()
            .get("/api/offenders/nomsNumber/NOMS1/courtReports")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtReport[].class);

        assertThat(courtReports).hasSize(3);
    }

    @Test
    public void missingOffenderRecordResultsInAllReportsForOffenderByNomsNotFound() {
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.empty());

        given()
                .when()
                .get("/api/offenders/nomsNumber/NOMS1/courtReports")
                .then()
                .statusCode(404);

    }


    @Test
    public void canGetSpecificReportForOffenderByNomsNumberAndReportId() {
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.of(1L));
        when(courtReportService.courtReportFor(any(), any())).thenReturn(
                Optional.of(CourtReport.builder().courtReportId(4L).offenderId(1L).build()));

        CourtReport courtReport = given()
            .when()
            .get("/api/offenders/nomsNumber/NOMS1/courtReports/4")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtReport.class);

        assertThat(courtReport).isNotNull();
        assertThat(courtReport.getCourtReportId()).isEqualTo(4);
    }


    @Test
    public void missingOffenderRecordResultsInReportForOffenderByNomsNumberAndReportIdNotFound() {
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.empty());

        given()
                .when()
                .get("/api/offenders/nomsNumber/NOMS1/courtReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void missingCourtReportRecordResultsInReportForOffenderByNomsNumberAndReportIdNotFound() {
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.of(1L));
        when(courtReportService.courtReportFor(any(), any())).thenReturn(Optional.empty());

        given()
                .when()
                .get("/api/offenders/nomsNumber/NOMS1/courtReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void canGetAllReportsForOffenderByOffenderId() {
        when(courtReportService.courtReportsFor(any())).thenReturn(ImmutableList.of(
                CourtReport.builder().courtReportId(1L).offenderId(1L).dateRequested(LocalDateTime.now()).build(),
                CourtReport.builder().courtReportId(2L).offenderId(1L).dateRequested(LocalDateTime.now()).build(),
                CourtReport.builder().courtReportId(4L).offenderId(1L).dateRequested(LocalDateTime.now()).build()
        ));

        CourtReport[] courtReports = given()
            .when()
            .get("/api/offenders/offenderId/1/courtReports")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtReport[].class);

        assertThat(courtReports).hasSize(3);
    }

    @Test
    public void canGetSpecificReportForOffenderByOffenderIdAndReportId() {
        when(courtReportService.courtReportFor(any(), any())).thenReturn(
                Optional.of(CourtReport.builder().courtReportId(4L).offenderId(1L).build()));

        CourtReport courtReport = given()
            .when()
            .get("/api/offenders/offenderId/1/courtReports/4")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtReport.class);

        assertThat(courtReport).isNotNull();
        assertThat(courtReport.getCourtReportId()).isEqualTo(4);
    }

    @Test
    public void missingCourtReportRecordResultsInReportForOffenderByOffenderIdAndReportIdNotFound() {
        when(courtReportService.courtReportFor(any(), any())).thenReturn(Optional.empty());

        given()
                .when()
                .get("/api/offenders/offenderId/1/courtReports/4")
                .then()
                .statusCode(404);
    }

}

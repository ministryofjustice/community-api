package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.InstitutionalReport;
import uk.gov.justice.digital.delius.service.InstitutionalReportService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.config.ApplicationConfig.customiseObjectMapper;

@ExtendWith(MockitoExtension.class)
public class InstitutionalReportControllerTest {

    private final ObjectMapper objectMapper = customiseObjectMapper(new ObjectMapper());

    @Mock
    private OffenderService offenderService;

    @Mock
    private InstitutionalReportService institutionalReportService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new InstitutionalReportController(offenderService, institutionalReportService)
        );
        RestAssuredMockMvc.config = RestAssuredMockMvcConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void canGetAllReportsForOffenderByCrn() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(institutionalReportService.institutionalReportsFor(any())).thenReturn(ImmutableList.of(
                InstitutionalReport.builder().institutionalReportId(1L)
                        .offenderId(1L)
                        .conviction(aConviction(10L))
                        .build(),
                InstitutionalReport.builder().institutionalReportId(2L)
                        .offenderId(1L)
                        .conviction(aConviction(20L))
                        .build(),
                InstitutionalReport.builder().institutionalReportId(4L)
                        .offenderId(1L)
                        .conviction(aConviction(30L))
                        .build()
        ));

        InstitutionalReport[] institutionalReports = given()
            .when()
            .get("/api/offenders/crn/CRN1/institutionalReports")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(InstitutionalReport[].class);

        assertThat(institutionalReports).hasSize(3);
    }

    @Test
    public void missingOffenderRecordResultsInAllReportsForOffenderByCrnNotFound() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/crn/CRN1/institutionalReports")
            .then()
            .statusCode(404);

    }

    @Test
    public void canGetSpecificReportForOffenderByCrnAndReportId() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(institutionalReportService.institutionalReportFor(any(), any())).thenReturn(
                Optional.of(InstitutionalReport.builder().institutionalReportId(4L).offenderId(1L).build()));

        InstitutionalReport institutionalReport = given()
            .when()
            .get("/api/offenders/crn/CRN1/institutionalReports/4")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(InstitutionalReport.class);

        assertThat(institutionalReport).isNotNull();
        assertThat(institutionalReport.getInstitutionalReportId()).isEqualTo(4);
    }

    @Test
    public void missingOffenderRecordResultsInReportForOffenderByCrnAndReportIdNotFound() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.empty());

        given()
                .when()
                .get("/api/offenders/crn/CRN1/institutionalReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void missingInstitutionalReportRecordResultsInReportForOffenderByCrnAndReportIdNotFound() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(institutionalReportService.institutionalReportFor(any(), any())).thenReturn(Optional.empty());

        given()
                .when()
                .get("/api/offenders/crn/CRN1/institutionalReports/4")
                .then()
                .statusCode(404);
    }

    private Conviction aConviction(Long id) {
        return Conviction.builder()
            .convictionId(id)
            .convictionDate(LocalDate.now())
            .active(true)
            .build();
    }
}

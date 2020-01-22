package uk.gov.justice.digital.delius.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.InstitutionalReport;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.InstitutionalReportService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class InstitutionalReportAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    @MockBean
    private OffenderService offenderService;

    @MockBean
    private InstitutionalReportService institutionalReportService;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
            new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.of(1L));
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
        when(institutionalReportService.institutionalReportFor(any(), any())).thenReturn(
                Optional.of(InstitutionalReport.builder().institutionalReportId(4L).offenderId(1L).build()));
    }

    @Test
    public void canGetAllReportsForOffenderByCrn() {

        InstitutionalReport[] institutionalReports = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/institutionalReports")
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
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/institutionalReports")
            .then()
            .statusCode(404);

    }

    @Test
    public void canGetSpecificReportForOffenderByCrnAndReportId() {

        InstitutionalReport institutionalReport = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/institutionalReports/4")
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
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/crn/CRN1/institutionalReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void missingInstitutionalReportRecordResultsInReportForOffenderByCrnAndReportIdNotFound() {
        when(institutionalReportService.institutionalReportFor(any(), any())).thenReturn(Optional.empty());

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/crn/CRN1/institutionalReports/4")
                .then()
                .statusCode(404);
    }


    @Test
    public void canGetAllReportsForOffenderByNomsNumber() {

        InstitutionalReport[] institutionalReports = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/NOMS1/institutionalReports")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(InstitutionalReport[].class);

        assertThat(institutionalReports).hasSize(3);
    }

    @Test
    public void missingOffenderRecordResultsInAllReportsForOffenderByNomsNotFound() {
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.empty());

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/nomsNumber/NOMS1/institutionalReports")
                .then()
                .statusCode(404);

    }


    @Test
    public void canGetSpecificReportForOffenderByNomsNumberAndReportId() {

        InstitutionalReport institutionalReport = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/NOMS1/institutionalReports/4")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(InstitutionalReport.class);

        assertThat(institutionalReport).isNotNull();
        assertThat(institutionalReport.getInstitutionalReportId()).isEqualTo(4);
    }


    @Test
    public void missingOffenderRecordResultsInReportForOffenderByNomsNumberAndReportIdNotFound() {
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.empty());

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/nomsNumber/NOMS1/institutionalReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void missingInstitutionalReportRecordResultsInReportForOffenderByNomsNumberAndReportIdNotFound() {
        when(institutionalReportService.institutionalReportFor(any(), any())).thenReturn(Optional.empty());

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/nomsNumber/NOMS1/institutionalReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void canGetAllReportsForOffenderByOffenderId() {

        InstitutionalReport[] institutionalReports = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/institutionalReports")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(InstitutionalReport[].class);

        assertThat(institutionalReports).hasSize(3);
    }

    @Test
    public void canGetSpecificReportForOffenderByOffenderIdAndReportId() {

        InstitutionalReport institutionalReport = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/institutionalReports/4")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(InstitutionalReport.class);

        assertThat(institutionalReport).isNotNull();
        assertThat(institutionalReport.getInstitutionalReportId()).isEqualTo(4);
    }

    @Test
    public void missingInstitutionalReportRecordResultsInReportForOffenderByOffenderIdAndReportIdNotFound() {
        when(institutionalReportService.institutionalReportFor(any(), any())).thenReturn(Optional.empty());

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/offenderId/1/institutionalReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void cannotGetReportForOffenderByOffenderIdAndReportIdWithoutJwtAuthorizationHeader() {
        RestAssured.when()
                .get("offenders/offenderId/1/institutionalReports/4")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void cannotGetReportsForOffenderByOffenderIdWithoutJwtAuthorizationHeader() {
        RestAssured.when()
                .get("offenders/offenderId/1/institutionalReports")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void cannotGetReportForOffenderByCrnAndReportIdWithoutJwtAuthorizationHeader() {
        RestAssured.when()
                .get("offenders/crn/CRN1/institutionalReports/4")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void cannotGetReportsForOffenderByCrnWithoutJwtAuthorizationHeader() {
        RestAssured.when()
                .get("offenders/crn/CRN1/institutionalReports")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void cannotGetReportForOffenderByNomsNumberAndReportIdWithoutJwtAuthorizationHeader() {
        RestAssured.when()
                .get("offenders/nomsNumber/NOMS1/institutionalReports/4")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void cannotGetReportsForOffenderByNomsNumberWithoutJwtAuthorizationHeader() {
        RestAssured.when()
                .get("offenders/nomsNumber/NOMS1/institutionalReports")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }

    private Conviction aConviction(Long id) {
        return Conviction.builder()
            .convictionId(id)
            .convictionDate(LocalDate.now())
            .active(true)
            .build();
    }
}

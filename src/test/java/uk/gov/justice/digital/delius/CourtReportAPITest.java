package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtReportRepository;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.CourtReportService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.user.UserData;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class CourtReportAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jwt jwt;

    @MockBean
    private OffenderService offenderService;

    @MockBean
    private CourtReportService courtReportService;

    @MockBean
    private CourtReportRepository courtReportRepository;



    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(offenderService.offenderIdOfNomsNumber("NOMS1")).thenReturn(Optional.of(1L));
        when(courtReportService.courtReportsFor(any())).thenReturn(ImmutableList.of(
            CourtReport.builder().courtReportId(1L).offenderId(1L).dateRequested(LocalDateTime.now()).build(),
            CourtReport.builder().courtReportId(2L).offenderId(1L).dateRequested(LocalDateTime.now()).build(),
            CourtReport.builder().courtReportId(4L).offenderId(1L).dateRequested(LocalDateTime.now()).build()
        ));
        when(courtReportService.courtReportFor(any(), any())).thenReturn(
                Optional.of(CourtReport.builder().courtReportId(4L).offenderId(1L).build()));
    }

    @Test
    public void canGetAllReportsForOffenderByCrn() {

        CourtReport[] courtReports = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/courtReports")
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
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/courtReports")
            .then()
            .statusCode(404);

    }

    @Test
    public void canGetSpecificReportForOffenderByCrnAndReportId() {

        CourtReport courtReport = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/CRN1/courtReports/4")
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
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/crn/CRN1/courtReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void missingCourtReportRecordResultsInReportForOffenderByCrnAndReportIdNotFound() {
        when(courtReportService.courtReportFor(any(), any())).thenReturn(Optional.empty());

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/crn/CRN1/courtReports/4")
                .then()
                .statusCode(404);
    }


    @Test
    public void canGetAllReportsForOffenderByNomsNumber() {

        CourtReport[] courtReports = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/NOMS1/courtReports")
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
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/nomsNumber/NOMS1/courtReports")
                .then()
                .statusCode(404);

    }


    @Test
    public void canGetSpecificReportForOffenderByNomsNumberAndReportId() {

        CourtReport courtReport = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/NOMS1/courtReports/4")
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
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/nomsNumber/NOMS1/courtReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void missingCourtReportRecordResultsInReportForOffenderByNomsNumberAndReportIdNotFound() {
        when(courtReportService.courtReportFor(any(), any())).thenReturn(Optional.empty());

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/nomsNumber/NOMS1/courtReports/4")
                .then()
                .statusCode(404);
    }

    @Test
    public void canGetAllReportsForOffenderByOffenderId() {

        CourtReport[] courtReports = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/courtReports")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtReport[].class);

        assertThat(courtReports).hasSize(3);
    }

    @Test
    public void canGetSpecificReportForOffenderByOffenderIdAndReportId() {

        CourtReport courtReport = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/courtReports/4")
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
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/offenderId/1/courtReports/4")
                .then()
                .statusCode(404);
    }



    private String aValidToken() {
        return aValidTokenFor(UUID.randomUUID().toString());
    }

    private String aValidTokenFor(String distinguishedName) {
        return "Bearer " + jwt.buildToken(UserData.builder()
                .distinguishedName(distinguishedName)
                .uid("bobby.davro").build());
    }
}
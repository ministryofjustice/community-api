package uk.gov.justice.digital.delius.controller.api;

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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Court;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.service.CourtAppearanceService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.user.UserData;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class CourtAPITest {

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourtAppearanceService courtAppearanceService;

    @MockBean
    private OffenderService offenderService;

    @Autowired
    private Jwt jwt;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));

        when(offenderService.offenderIdOfCrn("crn1")).thenReturn(Optional.of(1L));
        when(offenderService.offenderIdOfNomsNumber("noms1")).thenReturn(Optional.of(1L));
        when(offenderService.getOffenderByOffenderId(1L))
            .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(courtAppearanceService.courtAppearancesFor(1L))
            .thenReturn(ImmutableList.of(aCourtAppearance(1L), aCourtAppearance(2L)));
    }

    @Test
    public void canGetCourtAppearancesByCrn() {

        CourtAppearance[] courtAppearances = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/crn1/courtAppearances")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtAppearance[].class);

        assertThat(courtAppearances).hasSize(2);
        assertThat(courtAppearances[0].getCourt()).isNotNull();
        assertThat(courtAppearances[1].getCourt()).isNotNull();
        assertThat(courtAppearances[0].getCourtReports()).isNotNull();
        assertThat(courtAppearances[1].getCourtReports()).isNotNull();
    }

    @Test
    public void canGetCourtAppearancesByNoms() {

        CourtAppearance[] courtAppearances = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/noms1/courtAppearances")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtAppearance[].class);

        assertThat(courtAppearances).hasSize(2);
        assertThat(courtAppearances[0].getCourt()).isNotNull();
        assertThat(courtAppearances[1].getCourt()).isNotNull();
        assertThat(courtAppearances[0].getCourtReports()).isNotNull();
        assertThat(courtAppearances[1].getCourtReports()).isNotNull();
    }

    @Test
    public void canGetCourtAppearancesByOffenderId() {

        CourtAppearance[] courtAppearances = given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/1/courtAppearances")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(CourtAppearance[].class);

        assertThat(courtAppearances).hasSize(2);
        assertThat(courtAppearances[0].getCourt()).isNotNull();
        assertThat(courtAppearances[1].getCourt()).isNotNull();
        assertThat(courtAppearances[0].getCourtReports()).isNotNull();
        assertThat(courtAppearances[1].getCourtReports()).isNotNull();
    }

    @Test
    public void getCourtAppearancesForUnknownCrnReturnsNotFound() {
        when(offenderService.offenderIdOfCrn("notFoundCrn")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/crn/notFoundCrn/courtAppearances")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getCourtAppearancesForUnknownNomsNumberReturnsNotFound() {
        when(offenderService.offenderIdOfNomsNumber("notFoundNomsNumber")).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/nomsNumber/notFoundNomsNumber/courtAppearances")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfNomsNumber("notFoundNomsNumber");
    }

    @Test
    public void getCourtAppearancesForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .header("Authorization", aValidToken())
            .when()
            .get("offenders/offenderId/99/courtAppearances")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    @Test
    public void courtAppearancesByCrnMustHaveValidJwt() {
        given()
            .when()
            .get("offenders/crn/crn1/courtAppearances")
            .then()
            .statusCode(401);
    }

    private CourtAppearance aCourtAppearance(Long id) {
        return CourtAppearance.builder()
            .courtAppearanceId(id)
            .court(Court.builder().build())
            .courtReports(ImmutableList.of(CourtReport.builder().build()))
            .build();
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

package uk.gov.justice.digital.delius.controller.api;

import com.google.common.collect.ImmutableList;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Court;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.service.CourtAppearanceService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtControllerTest {

    @Mock
    private CourtAppearanceService courtAppearanceService;

    @Mock
    private OffenderService offenderService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new CourtController(offenderService, courtAppearanceService)
        );
    }

    @Test
    public void canGetCourtAppearancesByCrn() {
        when(offenderService.offenderIdOfCrn("crn1")).thenReturn(Optional.of(1L));
        when(courtAppearanceService.courtAppearancesFor(1L))
                .thenReturn(ImmutableList.of(aCourtAppearance(1L), aCourtAppearance(2L)));

        CourtAppearance[] courtAppearances = given()
            .when()
            .get("/api/offenders/crn/crn1/courtAppearances")
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
        when(offenderService.getOffenderByOffenderId(1L))
                .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(courtAppearanceService.courtAppearancesFor(1L))
                .thenReturn(ImmutableList.of(aCourtAppearance(1L), aCourtAppearance(2L)));

        CourtAppearance[] courtAppearances = given()
            .when()
            .get("/api/offenders/offenderId/1/courtAppearances")
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
            .when()
            .get("/api/offenders/crn/notFoundCrn/courtAppearances")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getCourtAppearancesForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/offenderId/99/courtAppearances")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    private CourtAppearance aCourtAppearance(Long id) {
        return CourtAppearance.builder()
            .courtAppearanceId(id)
            .court(Court.builder().build())
            .courtReports(ImmutableList.of(CourtReport.builder().build()))
            .build();
    }
}

package uk.gov.justice.digital.delius.controller.api;

import com.google.common.collect.ImmutableList;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.data.api.OffenceDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.service.OffenceService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffencesControllerTest {

    @Mock
    private OffenceService offenceService;

    @Mock
    private OffenderService offenderService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new OffenceController(offenderService, offenceService)
        );
    }

    @Test
    public void canGetOffencesByCrn() {
        when(offenderService.offenderIdOfCrn("crn1")).thenReturn(Optional.of(1L));
        when(offenceService.offencesFor(1L))
                .thenReturn(ImmutableList.of(anOffence(1L, "Fraud"), anOffence(2L, "Perjury")));

        Offence[] offences = given()
            .when()
            .get("/api/offenders/crn/crn1/offences")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Offence[].class);

        assertThat(offences).hasSize(2);
        assertThat(offences[0].getOffenceId()).isEqualTo("1");
        assertThat(offences[1].getOffenceId()).isEqualTo("2");
        assertThat(offences[0].getDetail().getDescription()).isEqualTo("Fraud");
        assertThat(offences[1].getDetail().getDescription()).isEqualTo("Perjury");
    }

    @Test
    public void canGetOffencesByOffenderId() {
        when(offenderService.getOffenderByOffenderId(1L))
                .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(offenceService.offencesFor(1L))
                .thenReturn(ImmutableList.of(anOffence(1L, "Fraud"), anOffence(2L, "Perjury")));

        Offence[] offences = given()
            .when()
            .get("/api/offenders/offenderId/1/offences")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Offence[].class);

        assertThat(offences).hasSize(2);
        assertThat(offences[0].getOffenceId()).isEqualTo("1");
        assertThat(offences[1].getOffenceId()).isEqualTo("2");
        assertThat(offences[0].getDetail().getDescription()).isEqualTo("Fraud");
        assertThat(offences[1].getDetail().getDescription()).isEqualTo("Perjury");
    }

    @Test
    public void getOffencesForUnknownCrnReturnsNotFound() {
        when(offenderService.offenderIdOfCrn("notFoundCrn")).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/crn/notFoundCrn/offences")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getOffencesForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/offenderId/99/offences")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    private Offence anOffence(Long id, String description) {
        return Offence.builder()
            .offenceId(id.toString())
            .detail(OffenceDetail.builder().code("001").description(description).build())
            .build();
    }
}

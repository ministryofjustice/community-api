package uk.gov.justice.digital.delius.controller.api;

import com.google.common.collect.ImmutableList;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConvictionControllerTest {

    @Mock
    private ConvictionService convictionService;

    @Mock
    private OffenderService offenderService;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new ConvictionController(offenderService, convictionService)
        );
    }

    @Test
    public void canGetConvictionsByOffenderId() {
        when(offenderService.getOffenderByOffenderId(1L))
                .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(convictionService.convictionsFor(1L, false))
                .thenReturn(ImmutableList.of(aConviction(2L), aConviction(1L)));

        Conviction[] convictions = given()
            .when()
            .get("/api/offenders/offenderId/1/convictions")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Conviction[].class);

        assertThat(convictions).hasSize(2);
        assertThat(convictions[0].getConvictionId()).isEqualTo(2L);
        assertThat(convictions[1].getConvictionId()).isEqualTo(1L);
    }

    @Test
    public void getConvictionsForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/offenderId/99/convictions")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    private Conviction aConviction(Long id) {
        return Conviction.builder()
            .convictionId(id)
            .build();
    }
}

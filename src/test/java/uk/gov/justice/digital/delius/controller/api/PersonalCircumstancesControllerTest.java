package uk.gov.justice.digital.delius.controller.api;

import com.google.common.collect.ImmutableList;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.PersonalCircumstance;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.PersonalCircumstanceService;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonalCircumstancesControllerTest {

    @Mock
    private PersonalCircumstanceService personalCircumstanceService;

    @Mock
    private OffenderService offenderService;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new PersonalCircumstanceController(offenderService, personalCircumstanceService)
        );
    }

    @Test
    public void canGetPersonalCircumstancesByCrn() {
        when(offenderService.offenderIdOfCrn("CRN1")).thenReturn(Optional.of(1L));
        when(personalCircumstanceService.personalCircumstancesFor(1L))
                .thenReturn(ImmutableList.of(aPersonalCircumstance(2L, "Benefit", "Universal Benefit"), aPersonalCircumstance(1L, "Accommodation", "Approved Premises")));

        PersonalCircumstance[] personalCircumstances = given()
            .when()
            .get("/api/offenders/crn/CRN1/personalCircumstances")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(PersonalCircumstance[].class);

        assertThat(personalCircumstances).hasSize(2);
        assertThat(personalCircumstances[0].getPersonalCircumstanceType().getDescription()).isEqualTo("Benefit");
        assertThat(personalCircumstances[0].getPersonalCircumstanceSubType().getDescription()).isEqualTo("Universal Benefit");
        assertThat(personalCircumstances[1].getPersonalCircumstanceType().getDescription()).isEqualTo("Accommodation");
        assertThat(personalCircumstances[1].getPersonalCircumstanceSubType().getDescription()).isEqualTo("Approved Premises");
    }

    @Test
    public void canGetPersonalCircumstancesByOffenderId() {
        when(offenderService.getOffenderByOffenderId(1L))
                .thenReturn(Optional.of(OffenderDetail.builder().offenderId(1L).build()));
        when(personalCircumstanceService.personalCircumstancesFor(1L))
                .thenReturn(ImmutableList.of(aPersonalCircumstance(2L, "Benefit", "Universal Benefit"), aPersonalCircumstance(1L, "Accommodation", "Approved Premises")));

        PersonalCircumstance[] personalCircumstances = given()
            .when()
            .get("/api/offenders/offenderId/1/personalCircumstances")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(PersonalCircumstance[].class);

        assertThat(personalCircumstances).hasSize(2);
        assertThat(personalCircumstances[0].getPersonalCircumstanceType().getDescription()).isEqualTo("Benefit");
        assertThat(personalCircumstances[0].getPersonalCircumstanceSubType().getDescription()).isEqualTo("Universal Benefit");
        assertThat(personalCircumstances[1].getPersonalCircumstanceType().getDescription()).isEqualTo("Accommodation");
        assertThat(personalCircumstances[1].getPersonalCircumstanceSubType().getDescription()).isEqualTo("Approved Premises");
    }

    @Test
    public void getPersonalCircumstancesForUnknownCrnReturnsNotFound() {
        when(offenderService.offenderIdOfCrn("notFoundCrn")).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/crn/notFoundCrn/personalCircumstances")
            .then()
            .statusCode(404);

        verify(offenderService).offenderIdOfCrn("notFoundCrn");
    }

    @Test
    public void getPersonalCircumstancesForUnknownOffenderIdReturnsNotFound() {
        when(offenderService.getOffenderByOffenderId(99L)).thenReturn(Optional.empty());

        given()
            .when()
            .get("/api/offenders/offenderId/99/personalCircumstances")
            .then()
            .statusCode(404);

        verify(offenderService).getOffenderByOffenderId(99L);
    }

    private PersonalCircumstance aPersonalCircumstance(Long id, String typeDescription, String subTypeDescription) {
        return PersonalCircumstance.builder()
            .personalCircumstanceId(id)
            .personalCircumstanceType(KeyValue.builder().code("X").description(typeDescription).build())
            .personalCircumstanceSubType(KeyValue.builder().code("X").description(subTypeDescription).build())
            .build();
    }
}

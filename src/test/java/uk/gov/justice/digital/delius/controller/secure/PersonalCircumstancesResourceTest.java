package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.PersonalCircumstance;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.PersonalCircumstanceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class PersonalCircumstancesResourceTest {

    private final OffenderService offenderService = mock(OffenderService.class);
    private final PersonalCircumstanceService personalCircumstanceService = mock(PersonalCircumstanceService.class);


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new PersonalCircumstanceResource(offenderService, personalCircumstanceService),
                new SecureControllerAdvice()
        );
    }

    @Nested
    @DisplayName("getOffenderPersonalCircumstancesByOffenderId")
    class GetOffenderPersonalCircumstancesByOffenderId {
        @BeforeEach
        void setUp() {
            when(offenderService.getOffenderByOffenderId(any()))
                    .thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
            when(personalCircumstanceService.personalCircumstancesFor(any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.getOffenderByOffenderId(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/offenderId/{offenderId}/personalCircumstances", "99")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).getOffenderByOffenderId(99L);
        }


        @Test
        @DisplayName("Will return 200 OK even when offender has no registered personal circumstances")
        void WillReturn200EvenOffenderHasNoPersonalCircumstances() {
            when(personalCircumstanceService.personalCircumstancesFor(any())).thenReturn(List.of());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/offenderId/{offenderId}/personalCircumstances", "99")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each personal circumstance registered")
        void WillReturnEachPersonalCircumstance() {
            when(personalCircumstanceService.personalCircumstancesFor(any())).thenReturn(List.of());
            when(personalCircumstanceService.personalCircumstancesFor(any()))
                    .thenReturn(List.of(
                            PersonalCircumstance
                                    .builder()
                                    .personalCircumstanceId(2500064995L)
                                    .offenderId(99L)
                                    .personalCircumstanceSubType(KeyValue
                                            .builder()
                                            .code("APMP1")
                                            .description("MiP approved")
                                            .build())
                                    .personalCircumstanceType(KeyValue
                                            .builder()
                                            .code("APMP")
                                            .description("AP - Medication in Posession  - Assessment")
                                            .build())
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .endDate(LocalDate.parse("2020-09-11"))
                                    .evidenced(true)
                                    .build(),
                            PersonalCircumstance
                                    .builder()
                                    .personalCircumstanceId(2500064996L)
                                    .offenderId(99L)
                                    .personalCircumstanceSubType(KeyValue
                                            .builder()
                                            .code("ACCP1")
                                            .description("Transient/short term accommodation")
                                            .build())
                                    .personalCircumstanceType(KeyValue
                                            .builder()
                                            .code("ACCP")
                                            .description("Accommodation")
                                            .build())
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .evidenced(false)
                                    .build()
                    ));


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/offenderId/{offenderId}/personalCircumstances", "99")
                    .then()
                    .statusCode(200)
                    .body("personalCircumstances[0].personalCircumstanceId", is(2500064995L))
                    .body("personalCircumstances[0].offenderId", is(99))
                    .body("personalCircumstances[0].personalCircumstanceSubType.code", is("APMP1"))
                    .body("personalCircumstances[0].personalCircumstanceSubType.description", is("MiP approved"))
                    .body("personalCircumstances[0].personalCircumstanceType.code", is("APMP"))
                    .body("personalCircumstances[0].personalCircumstanceType.description", is("AP - Medication in Posession  - Assessment"))
                    .body("personalCircumstances[0].startDate", is("2019-09-11"))
                    .body("personalCircumstances[0].endDate", is("2020-09-11"))
                    .body("personalCircumstances[0].evidenced", is(true))
                    .body("personalCircumstances[1].personalCircumstanceId", is(2500064996L))
                    .body("personalCircumstances[1].endDate", nullValue());
        }
    }

    @Nested
    @DisplayName("getOffenderPersonalCircumstancesByNomsNumber")
    class GetOffenderPersonalCircumstancesByNomsNumber {
        @BeforeEach
        void setUp() {
            when(offenderService.offenderIdOfNomsNumber(any()))
                    .thenReturn(Optional.of(99L));
            when(personalCircumstanceService.personalCircumstancesFor(any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.offenderIdOfNomsNumber(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/personalCircumstances", "G9542VP")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).offenderIdOfNomsNumber("G9542VP");
        }


        @Test
        @DisplayName("Will return 200 OK even when offender has no registered personal circumstances")
        void WillReturn200EvenOffenderHasNoPersonalCircumstances() {
            when(personalCircumstanceService.personalCircumstancesFor(any())).thenReturn(List.of());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/personalCircumstances", "G9542VP")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each personal circumstance registered")
        void WillReturnEachPersonalCircumstance() {
            when(personalCircumstanceService.personalCircumstancesFor(any())).thenReturn(List.of());
            when(personalCircumstanceService.personalCircumstancesFor(any()))
                    .thenReturn(List.of(
                            PersonalCircumstance
                                    .builder()
                                    .personalCircumstanceId(2500064995L)
                                    .offenderId(99L)
                                    .personalCircumstanceSubType(KeyValue
                                            .builder()
                                            .code("APMP1")
                                            .description("MiP approved")
                                            .build())
                                    .personalCircumstanceType(KeyValue
                                            .builder()
                                            .code("APMP")
                                            .description("AP - Medication in Posession  - Assessment")
                                            .build())
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .endDate(LocalDate.parse("2020-09-11"))
                                    .evidenced(true)
                                    .build(),
                            PersonalCircumstance
                                    .builder()
                                    .personalCircumstanceId(2500064996L)
                                    .offenderId(99L)
                                    .personalCircumstanceSubType(KeyValue
                                            .builder()
                                            .code("ACCP1")
                                            .description("Transient/short term accommodation")
                                            .build())
                                    .personalCircumstanceType(KeyValue
                                            .builder()
                                            .code("ACCP")
                                            .description("Accommodation")
                                            .build())
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .evidenced(false)
                                    .build()
                    ));


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/personalCircumstances", "G9542VP")
                    .then()
                    .statusCode(200)
                    .body("personalCircumstances[0].personalCircumstanceId", is(2500064995L))
                    .body("personalCircumstances[0].offenderId", is(99))
                    .body("personalCircumstances[0].personalCircumstanceSubType.code", is("APMP1"))
                    .body("personalCircumstances[0].personalCircumstanceSubType.description", is("MiP approved"))
                    .body("personalCircumstances[0].personalCircumstanceType.code", is("APMP"))
                    .body("personalCircumstances[0].personalCircumstanceType.description", is("AP - Medication in Posession  - Assessment"))
                    .body("personalCircumstances[0].startDate", is("2019-09-11"))
                    .body("personalCircumstances[0].endDate", is("2020-09-11"))
                    .body("personalCircumstances[0].evidenced", is(true))
                    .body("personalCircumstances[1].personalCircumstanceId", is(2500064996L))
                    .body("personalCircumstances[1].endDate", nullValue());
        }
    }

    @Nested
    @DisplayName("getOffenderPersonalCircumstancesByCrn")
    class GetOffenderPersonalCircumstancesByCrn {
        @BeforeEach
        void setUp() {
            when(offenderService.offenderIdOfCrn(any()))
                    .thenReturn(Optional.of(99L));
            when(personalCircumstanceService.personalCircumstancesFor(any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.offenderIdOfCrn(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/personalCircumstances", "X12345")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).offenderIdOfCrn("X12345");
        }


        @Test
        @DisplayName("Will return 200 OK even when offender has no registered personal circumstances")
        void WillReturn200EvenOffenderHasNoPersonalCircumstances() {
            when(personalCircumstanceService.personalCircumstancesFor(any())).thenReturn(List.of());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/personalCircumstances", "X12345")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each personal circumstance registered")
        void WillReturnEachPersonalCircumstance() {
            when(personalCircumstanceService.personalCircumstancesFor(any())).thenReturn(List.of());
            when(personalCircumstanceService.personalCircumstancesFor(any()))
                    .thenReturn(List.of(
                            PersonalCircumstance
                                    .builder()
                                    .personalCircumstanceId(2500064995L)
                                    .offenderId(99L)
                                    .personalCircumstanceSubType(KeyValue
                                            .builder()
                                            .code("APMP1")
                                            .description("MiP approved")
                                            .build())
                                    .personalCircumstanceType(KeyValue
                                            .builder()
                                            .code("APMP")
                                            .description("AP - Medication in Posession  - Assessment")
                                            .build())
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .endDate(LocalDate.parse("2020-09-11"))
                                    .evidenced(true)
                                    .build(),
                            PersonalCircumstance
                                    .builder()
                                    .personalCircumstanceId(2500064996L)
                                    .offenderId(99L)
                                    .personalCircumstanceSubType(KeyValue
                                            .builder()
                                            .code("ACCP1")
                                            .description("Transient/short term accommodation")
                                            .build())
                                    .personalCircumstanceType(KeyValue
                                            .builder()
                                            .code("ACCP")
                                            .description("Accommodation")
                                            .build())
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .evidenced(false)
                                    .build()
                    ));


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/personalCircumstances", "X12345")
                    .then()
                    .statusCode(200)
                    .body("personalCircumstances[0].personalCircumstanceId", is(2500064995L))
                    .body("personalCircumstances[0].offenderId", is(99))
                    .body("personalCircumstances[0].personalCircumstanceSubType.code", is("APMP1"))
                    .body("personalCircumstances[0].personalCircumstanceSubType.description", is("MiP approved"))
                    .body("personalCircumstances[0].personalCircumstanceType.code", is("APMP"))
                    .body("personalCircumstances[0].personalCircumstanceType.description", is("AP - Medication in Posession  - Assessment"))
                    .body("personalCircumstances[0].startDate", is("2019-09-11"))
                    .body("personalCircumstances[0].endDate", is("2020-09-11"))
                    .body("personalCircumstances[0].evidenced", is(true))
                    .body("personalCircumstances[1].personalCircumstanceId", is(2500064996L))
                    .body("personalCircumstances[1].endDate", nullValue());
        }
    }

}
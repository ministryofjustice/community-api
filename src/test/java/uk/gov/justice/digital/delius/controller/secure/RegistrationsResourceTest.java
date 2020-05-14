package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.Registration;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RegistrationService;

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

class RegistrationsResourceTest {

    private final OffenderService offenderService = mock(OffenderService.class);
    private final RegistrationService registrationService = mock(RegistrationService.class);


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new RegistrationResource(offenderService, registrationService),
                new SecureControllerAdvice()
        );
    }

    @Nested
    @DisplayName("getOffenderRegistrationsByOffenderId")
    class GetOffenderRegistrationsByOffenderId {
        @BeforeEach
        void setUp() {
            when(offenderService.getOffenderByOffenderId(any()))
                    .thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
            when(registrationService.registrationsFor(any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.getOffenderByOffenderId(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/offenderId/{offenderId}/registrations", "99")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).getOffenderByOffenderId(99L);
        }


        @Test
        @DisplayName("Will return 200 OK even when offender has no registered registrations")
        void WillReturn200EvenOffenderHasNoRegistrations() {
            when(registrationService.registrationsFor(any())).thenReturn(List.of());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/offenderId/{offenderId}/registrations", "99")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each registration registered")
        void WillReturnEachRegistration() {
            when(registrationService.registrationsFor(any())).thenReturn(List.of());
            when(registrationService.registrationsFor(any()))
                    .thenReturn(List.of(
                            Registration
                                    .builder()
                                    .registrationId(2500064995L)
                                    .offenderId(99L)
                                    .register(KeyValue
                                            .builder()
                                            .code("5")
                                            .description("Public Protection")
                                            .build())
                                    .type(KeyValue
                                            .builder()
                                            .code("REG15")
                                            .description("Risk to Known Adult")
                                            .build())
                                    .riskColour("Red")
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .nextReviewDate(LocalDate.parse("2020-12-11"))
                                    .notes("Next review should be the final one")
                                    .reviewPeriodMonths(6L)
                                    .active(true)
                                    .registeringTeam(KeyValue
                                            .builder()
                                            .code("N02T01")
                                            .description("OMU A")
                                            .build())
                                    .registeringOfficer(Human
                                            .builder()
                                            .forenames("Sandra Karen")
                                            .surname("Kane")
                                            .build())
                                    .registeringProbationArea(KeyValue
                                            .builder()
                                            .code("N02")
                                            .description("NPS North East")
                                            .build())
                                    .warnUser(true)
                                    .build(),
                            Registration
                                    .builder()
                                    .registrationId(2500064996L)
                                    .offenderId(99L)
                                    .register(KeyValue
                                            .builder()
                                            .code("1")
                                            .description("RoSH")
                                            .build())
                                    .type(KeyValue
                                            .builder()
                                            .code("RLRH")
                                            .description("Low RoSH")
                                            .build())
                                    .riskColour("Green")
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .nextReviewDate(LocalDate.parse("2020-12-11"))
                                    .notes("Next review should be the final one")
                                    .reviewPeriodMonths(6L)
                                    .active(true)
                                    .registeringTeam(KeyValue
                                            .builder()
                                            .code("N02T01")
                                            .description("OMU A")
                                            .build())
                                    .registeringOfficer(Human
                                            .builder()
                                            .forenames("Sandra Karen")
                                            .surname("Kane")
                                            .build())
                                    .registeringProbationArea(KeyValue
                                            .builder()
                                            .code("N02")
                                            .description("NPS North East")
                                            .build())
                                    .warnUser(false)
                                    .build()
                    ));


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/offenderId/{offenderId}/registrations", "99")
                    .then()
                    .statusCode(200)
                    .body("registrations[0].registrationId", is(2500064995L))
                    .body("registrations[0].offenderId", is(99))
                    .body("registrations[0].register.code", is("5"))
                    .body("registrations[0].register.description", is("Public Protection"))
                    .body("registrations[0].type.code", is("REG15"))
                    .body("registrations[0].type.description", is("Risk to Known Adult"))
                    .body("registrations[0].riskColour", is("Red"))
                    .body("registrations[0].startDate", is("2019-09-11"))
                    .body("registrations[0].nextReviewDate", is("2020-12-11"))
                    .body("registrations[0].notes", is("Next review should be the final one"))
                    .body("registrations[0].reviewPeriodMonths", is(6))
                    .body("registrations[0].active", is(true))
                    .body("registrations[0].registeringTeam.code", is("N02T01"))
                    .body("registrations[0].registeringTeam.description", is("OMU A"))
                    .body("registrations[0].registeringOfficer.forenames", is("Sandra Karen"))
                    .body("registrations[0].registeringOfficer.surname", is("Kane"))
                    .body("registrations[0].registeringProbationArea.code", is("N02"))
                    .body("registrations[0].registeringProbationArea.description", is("NPS North East"))
                    .body("registrations[0].endDate", nullValue())
                    .body("registrations[1].registrationId", is(2500064996L));
        }
    }

    @Nested
    @DisplayName("getOffenderRegistrationsByNomsNumber")
    class GetOffenderRegistrationsByNomsNumber {
        @BeforeEach
        void setUp() {
            when(offenderService.offenderIdOfNomsNumber(any()))
                    .thenReturn(Optional.of(99L));
            when(registrationService.registrationsFor(any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.offenderIdOfNomsNumber(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/registrations", "G9542VP")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).offenderIdOfNomsNumber("G9542VP");
        }


        @Test
        @DisplayName("Will return 200 OK even when offender has no registered registrations")
        void WillReturn200EvenOffenderHasNoRegistrations() {
            when(registrationService.registrationsFor(any())).thenReturn(List.of());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/registrations", "G9542VP")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each registration registered")
        void WillReturnEachRegistration() {
            when(registrationService.registrationsFor(any())).thenReturn(List.of());
            when(registrationService.registrationsFor(any()))
                    .thenReturn(List.of(
                            Registration
                                    .builder()
                                    .registrationId(2500064995L)
                                    .offenderId(99L)
                                    .register(KeyValue
                                            .builder()
                                            .code("5")
                                            .description("Public Protection")
                                            .build())
                                    .type(KeyValue
                                            .builder()
                                            .code("REG15")
                                            .description("Risk to Known Adult")
                                            .build())
                                    .riskColour("Red")
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .nextReviewDate(LocalDate.parse("2020-12-11"))
                                    .notes("Next review should be the final one")
                                    .reviewPeriodMonths(6L)
                                    .active(true)
                                    .registeringTeam(KeyValue
                                            .builder()
                                            .code("N02T01")
                                            .description("OMU A")
                                            .build())
                                    .registeringOfficer(Human
                                            .builder()
                                            .forenames("Sandra Karen")
                                            .surname("Kane")
                                            .build())
                                    .registeringProbationArea(KeyValue
                                            .builder()
                                            .code("N02")
                                            .description("NPS North East")
                                            .build())
                                    .warnUser(true)
                                    .build()
                    ));


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/registrations", "G9542VP")
                    .then()
                    .statusCode(200)
                    .body("registrations[0].registrationId", is(2500064995L));
        }
    }

    @Nested
    @DisplayName("getOffenderRegistrationsByCrn")
    class GetOffenderRegistrationsByCrn {
        @BeforeEach
        void setUp() {
            when(offenderService.offenderIdOfCrn(any()))
                    .thenReturn(Optional.of(99L));
            when(registrationService.registrationsFor(any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.offenderIdOfCrn(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/registrations", "X12345")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).offenderIdOfCrn("X12345");
        }


        @Test
        @DisplayName("Will return 200 OK even when offender has no registered registrations")
        void WillReturn200EvenOffenderHasNoRegistrations() {
            when(registrationService.registrationsFor(any())).thenReturn(List.of());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/registrations", "X12345")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each registration registered")
        void WillReturnEachRegistration() {
            when(registrationService.registrationsFor(any())).thenReturn(List.of());
            when(registrationService.registrationsFor(any()))
                    .thenReturn(List.of(
                            Registration
                                    .builder()
                                    .registrationId(2500064995L)
                                    .offenderId(99L)
                                    .register(KeyValue
                                            .builder()
                                            .code("5")
                                            .description("Public Protection")
                                            .build())
                                    .type(KeyValue
                                            .builder()
                                            .code("REG15")
                                            .description("Risk to Known Adult")
                                            .build())
                                    .riskColour("Red")
                                    .startDate(LocalDate.parse("2019-09-11"))
                                    .nextReviewDate(LocalDate.parse("2020-12-11"))
                                    .notes("Next review should be the final one")
                                    .reviewPeriodMonths(6L)
                                    .active(true)
                                    .registeringTeam(KeyValue
                                            .builder()
                                            .code("N02T01")
                                            .description("OMU A")
                                            .build())
                                    .registeringOfficer(Human
                                            .builder()
                                            .forenames("Sandra Karen")
                                            .surname("Kane")
                                            .build())
                                    .registeringProbationArea(KeyValue
                                            .builder()
                                            .code("N02")
                                            .description("NPS North East")
                                            .build())
                                    .warnUser(true)
                                    .build()

                    ));


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/registrations", "X12345")
                    .then()
                    .statusCode(200)
                    .body("registrations[0].registrationId", is(2500064995L));
        }
    }

}
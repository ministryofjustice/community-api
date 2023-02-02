package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.Registration;
import uk.gov.justice.digital.delius.data.api.RegistrationReview;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RegistrationService;
import uk.gov.justice.digital.delius.service.UserAccessService;

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
    private final UserAccessService userAccessService = mock(UserAccessService.class);


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new RegistrationResource(offenderService, registrationService, userAccessService),
                new SecureControllerAdvice()
        );
    }

    @Nested
    @DisplayName("getOffenderRegistrationsByNomsNumber")
    class GetOffenderRegistrationsByNomsNumber {
        @BeforeEach
        void setUp() {
            when(offenderService.mostLikelyOffenderIdOfNomsNumber(any())).thenReturn(Either.right(Optional.of(99L)));

            when(registrationService.registrationsFor(any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.mostLikelyOffenderIdOfNomsNumber(any())).thenReturn(Either.right(Optional.empty()));

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/registrations", "G9542VP")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).mostLikelyOffenderIdOfNomsNumber("G9542VP");
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
                                    .registeringOfficer(StaffHuman
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
                    .auth()
                    .authentication(mock(Authentication.class))
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
                    .auth()
                    .authentication(mock(Authentication.class))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/registrations", "X12345")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each registration registered")
        void WillReturnEachRegistration() {
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
                                    .registeringOfficer(StaffHuman
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
                    .auth()
                    .authentication(mock(Authentication.class))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/registrations", "X12345")
                    .then()
                    .statusCode(200)
                    .body("registrations[0].registrationId", is(2500064995L));
        }
    }

    @Nested
    @DisplayName("getOffenderRegistrationById")
    class GetOffenderRegistration {
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
                .auth()
                .authentication(mock(Authentication.class))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/offenders/crn/{X12345}/registrations/6789", "X12345")
                .then()
                .statusCode(404)
                .body("developerMessage", containsString("Offender with crn X12345 does not exist"));

            verify(offenderService).offenderIdOfCrn("X12345");
        }

        @Test
        @DisplayName("Will return 404 when registration not found")
        void WillReturn404WhenRegistrationNotFound() {
            when(offenderService.offenderIdOfCrn(any())).thenReturn(Optional.of(99L));

            when(registrationService.registration(99L, 6789L)).thenReturn(Optional.empty());

            given()
                .auth()
                .authentication(mock(Authentication.class))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/offenders/crn/{X12345}/registrations/{6789}", "X12345", "6789")
                .then()
                .statusCode(404)
                .body("developerMessage", containsString("No registration found with id 6789"));

            verify(offenderService).offenderIdOfCrn("X12345");
        }

        @Test
        @DisplayName("Will return the registration")
        void WillReturnRegistrationWithGivenId() {
            when(offenderService.offenderIdOfCrn(any())).thenReturn(Optional.of(99L));

            when(registrationService.registration(99L, 6789L))
                .thenReturn(
                    Optional.of(Registration
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
                        .registeringOfficer(StaffHuman
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
                        .registrationReviews(List.of(RegistrationReview.builder()
                            .completed(false)
                            .notes("Some review notes")
                            .reviewDate(LocalDate.parse("2021-08-20"))
                            .reviewDateDue(LocalDate.parse("2022-03-20"))
                            .reviewingOfficer(StaffHuman
                                .builder()
                                .forenames("Sandra Karen")
                                .surname("Kane")
                                .build()).reviewingTeam(KeyValue
                                .builder()
                                .code("N02T01")
                                .description("OMU A")
                                .build())
                            .build()))
                        .build()));


            given()
                .auth()
                .authentication(mock(Authentication.class))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/secure/offenders/crn/{X12345}/registrations/{6789}", "X12345", "6789")
                .then()
                .statusCode(200)
                .body("registrationId", is(2500064995L));
        }
    }
}

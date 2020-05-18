package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.AdditionalIdentifier;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderIdentifiers;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.List;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class OffenderIdentifiersResourceTest {

    private final OffenderService offenderService = mock(OffenderService.class);


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new OffenderIdentifiersResource(offenderService),
                new SecureControllerAdvice()
        );
        when(offenderService.getOffenderIdentifiers(any())).thenReturn(OffenderIdentifiers
                .builder()
                .offenderId(99L)
                .additionalIdentifiers(List.of())
                .primaryIdentifiers(IDs
                        .builder()
                        .crn("X1234")
                        .build())
                .build());
    }

    @Nested
    @DisplayName("getOffenderIdentifiersByOffenderId")
    class GetOffenderIdentifiersByOffenderId {
        @BeforeEach
        void setUp() {
            when(offenderService.getOffenderByOffenderId(any()))
                    .thenReturn(Optional.of(OffenderDetail.builder().offenderId(99L).build()));
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.getOffenderByOffenderId(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/offenderId/{offenderId}/identifiers", "99")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).getOffenderByOffenderId(99L);
        }


        @Test
        @DisplayName("Will return 200 OK even when offender has no additional identifiers")
        void WillReturn200EvenOffenderHasNoIdentifiers() {
            when(offenderService.getOffenderIdentifiers(any())).thenReturn(OffenderIdentifiers
                    .builder()
                    .offenderId(99L)
                    .additionalIdentifiers(List.of())
                    .primaryIdentifiers(IDs
                            .builder()
                            .crn("X1234")
                            .build())
                    .build());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/offenderId/{offenderId}/identifiers", "99")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each additional personal identifier")
        void WillReturnEachAdditionalPersonalIdentifier() {
            when(offenderService.getOffenderIdentifiers(any())).thenReturn(OffenderIdentifiers
                    .builder()
                    .offenderId(99L)
                    .additionalIdentifiers(List.of(
                            AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(22L)
                                    .value("A12345F")
                                    .type(KeyValue
                                            .builder()
                                            .code("DNOMS")
                                            .description("Duplicate NOMS number")
                                            .build())
                                    .build(),
                            AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(23L)
                                    .value("G12345F")
                                    .type(KeyValue
                                            .builder()
                                            .code("XNOMS")
                                            .description("Former NOMS number")
                                            .build())
                                    .build()
                    ))
                    .primaryIdentifiers(IDs
                            .builder()
                            .crn("X12345")
                            .nomsNumber("A32456J")
                            .mostRecentPrisonerNumber("B12345")
                            .pncNumber("2003/001282X")
                            .croNumber("02/1224")
                            .immigrationNumber("6543")
                            .niNumber("NE121212X")
                            .build())
                    .build());


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/offenderId/{offenderId}/identifiers", "99")
                    .then()
                    .statusCode(200)
                    .body("primaryIdentifiers.crn", is("X12345"))
                    .body("primaryIdentifiers.nomsNumber", is("A32456J"))
                    .body("primaryIdentifiers.mostRecentPrisonerNumber", is("B12345"))
                    .body("primaryIdentifiers.pncNumber", is("2003/001282X"))
                    .body("primaryIdentifiers.croNumber", is("02/1224"))
                    .body("primaryIdentifiers.immigrationNumber", is("6543"))
                    .body("primaryIdentifiers.niNumber", is("NE121212X"))
                    .body("additionalIdentifiers[0].additionalIdentifierId", is(22))
                    .body("additionalIdentifiers[0].value", is("A12345F"))
                    .body("additionalIdentifiers[0].type.code", is("DNOMS"))
                    .body("additionalIdentifiers[0].type.description", is("Duplicate NOMS number"))
                    .body("additionalIdentifiers[1].additionalIdentifierId", is(23));
        }
    }

    @Nested
    @DisplayName("getOffenderIdentifiersByNomsNumber")
    class GetOffenderIdentifiersByNomsNumber {
        @BeforeEach
        void setUp() {
            when(offenderService.offenderIdOfNomsNumber(any()))
                    .thenReturn(Optional.of(99L));
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.offenderIdOfNomsNumber(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/identifiers", "G9542VP")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).offenderIdOfNomsNumber("G9542VP");
        }


        @Test
        @DisplayName("Will return 200 OK even when offender has no additional identifiers")
        void WillReturn200EvenOffenderHasNoIdentifiers() {
            when(offenderService.getOffenderIdentifiers(any())).thenReturn(OffenderIdentifiers
                    .builder()
                    .offenderId(99L)
                    .additionalIdentifiers(List.of())
                    .primaryIdentifiers(IDs
                            .builder()
                            .crn("X1234")
                            .build())
                    .build());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/identifiers", "G9542VP")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each additional identifiers")
        void WillReturnAdditionalIdentifiers() {
            when(offenderService.getOffenderIdentifiers(any())).thenReturn(OffenderIdentifiers
                    .builder()
                    .offenderId(99L)
                    .additionalIdentifiers(List.of(
                            AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(22L)
                                    .value("A12345F")
                                    .type(KeyValue
                                            .builder()
                                            .code("DNOMS")
                                            .description("Duplicate NOMS number")
                                            .build())
                                    .build()
                    ))
                    .primaryIdentifiers(IDs
                            .builder()
                            .crn("X12345")
                            .build())
                    .build());


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nomsNumber/{nomsNumber}/identifiers", "G9542VP")
                    .then()
                    .statusCode(200)
                    .body("primaryIdentifiers.crn", is("X12345"))
                    .body("additionalIdentifiers[0].additionalIdentifierId", is(22));
        }
    }

    @Nested
    @DisplayName("getOffenderIdentifiersByCrn")
    class GetOffenderIdentifiersByCrn {
        @BeforeEach
        void setUp() {
            when(offenderService.offenderIdOfCrn(any()))
                    .thenReturn(Optional.of(99L));
        }

        @Test
        @DisplayName("Will return 404 when offender not found")
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.offenderIdOfCrn(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/identifiers", "X12345")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("No offender found"));

            verify(offenderService).offenderIdOfCrn("X12345");
        }


        @Test
        @DisplayName("Will return 200 OK even when offender has no additional identifiers")
        void WillReturn200EvenOffenderHasNoIdentifiers() {
            when(offenderService.getOffenderIdentifiers(any())).thenReturn(OffenderIdentifiers
                    .builder()
                    .offenderId(99L)
                    .additionalIdentifiers(List.of())
                    .primaryIdentifiers(IDs
                            .builder()
                            .crn("X1234")
                            .build())
                    .build());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/identifiers", "X12345")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return additional offender identifiers")
        void WillReturnAdditionalIdentifiers() {
            when(offenderService.getOffenderIdentifiers(any())).thenReturn(OffenderIdentifiers
                    .builder()
                    .offenderId(99L)
                    .additionalIdentifiers(List.of(
                            AdditionalIdentifier
                                    .builder()
                                    .additionalIdentifierId(22L)
                                    .value("A12345F")
                                    .type(KeyValue
                                            .builder()
                                            .code("DNOMS")
                                            .description("Duplicate NOMS number")
                                            .build())
                                    .build()
                    ))
                    .primaryIdentifiers(IDs
                            .builder()
                            .crn("X12345")
                            .build())
                    .build());


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/crn/{X12345}/identifiers", "X12345")
                    .then()
                    .statusCode(200)
                    .body("primaryIdentifiers.crn", is("X12345"))
                    .body("additionalIdentifiers[0].additionalIdentifierId", is(22));
        }
    }
}
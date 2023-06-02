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
package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ReferenceData;
import uk.gov.justice.digital.delius.service.ReferenceDataService;

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

class ReferenceDataResourceTest {

    private final ReferenceDataService referenceDataService = mock(ReferenceDataService.class);


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new ReferenceDataResource(referenceDataService),
                new SecureControllerAdvice()
        );
    }

    @Nested
    @DisplayName("getReferenceData")
    class GetReferenceData {
        @Test
        @DisplayName("Will return 404 when data set not found")
        void WillReturn404WhenDataSetNotFound() {
            when(referenceDataService.getReferenceDataForSet(any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/referenceData/set/{set}", "CHICKEN TYPES")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("Data set CHICKEN TYPES not found"));

            verify(referenceDataService).getReferenceDataForSet("CHICKEN TYPES");
        }


        @Test
        @DisplayName("Will return 200 OK even when data set list is empty")
        void WillReturn200EvenReferenceDataIsEmpty() {
            when(referenceDataService.getReferenceDataForSet(any())).thenReturn(Optional.of(List.of()));

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/referenceData/set/{set}", "ADDITIONAL IDENTIFIER TYPE")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Will return each reference data item")
        void WillReturnReferenceDataItem() {
            when(referenceDataService.getReferenceDataForSet(any()))
                    .thenReturn(Optional.of(List.of(
                            ReferenceData
                                    .builder()
                                    .active(true)
                                    .description("Lifer Number")
                                    .code("LIFN")
                                    .build(),
                            ReferenceData
                                    .builder()
                                    .active(false)
                                    .description("Other Personal Identifier")
                                    .code("OTHR")
                                    .build()
                    )));


            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/referenceData/set/{set}", "ADDITIONAL IDENTIFIER TYPE")
                    .then()
                    .statusCode(200)
                    .body("referenceData[0].active", is(true))
                    .body("referenceData[0].code", is("LIFN"))
                    .body("referenceData[0].description", is("Lifer Number"))
                    .body("referenceData[1].active", is(false))
                    .body("referenceData[1].code", is("OTHR"))
                    .body("referenceData[1].description", is("Other Personal Identifier"));
        }
    }

    @Nested
    @DisplayName("getReferenceDataSets")
    class GetReferenceDataSets {

        @Test
        @DisplayName("Will return each data set")
        void WillReturnAllDataSets() {
            when(referenceDataService.getReferenceDataSets())
                    .thenReturn(List.of(
                            KeyValue
                                    .builder()
                                    .code("AWAITING ARRIVAL REASON")
                                    .description("Awaiting Arrival Reason")
                                    .build(),
                            KeyValue
                                    .builder()
                                    .code("POM ALLOCATION REASON")
                                    .description("POM Allocation Reason")
                                    .build()
                    ));

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/referenceData/sets")
                    .then()
                    .statusCode(200)
                    .body("referenceDataSets[0].code", is("AWAITING ARRIVAL REASON"))
                    .body("referenceDataSets[0].description", is("Awaiting Arrival Reason"))
                    .body("referenceDataSets[1].code", is("POM ALLOCATION REASON"))
                    .body("referenceDataSets[1].description", is("POM Allocation Reason"));
        }
    }
}
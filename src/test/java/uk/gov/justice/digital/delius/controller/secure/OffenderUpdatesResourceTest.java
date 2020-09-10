package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.OffenderDelta;
import uk.gov.justice.digital.delius.service.OffenderDeltaLockedException;
import uk.gov.justice.digital.delius.service.OffenderUpdatesService;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class OffenderUpdatesResourceTest {

    private final OffenderUpdatesService offenderUpdatesService = mock(OffenderUpdatesService.class);


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new OffenderUpdatesResource(offenderUpdatesService),
                new SecureControllerAdvice()
        );
        when(offenderUpdatesService.getNextUpdate()).thenReturn(Optional.empty());
    }


    @Nested
    @DisplayName("offenders/nextUpdate")
    class GetNextUpdate {

        @Test
        @DisplayName("Will get the next update")
        void willGetNextUpdate() {
            when(offenderUpdatesService.getNextUpdate()).thenReturn(Optional.of(OffenderDelta
                    .builder()
                    .action("INSERT")
                    .dateChanged(LocalDateTime.parse("2012-01-31T14:23:12"))
                    .offenderDeltaId(1L)
                    .offenderId(99L)
                    .sourceRecordId(101L)
                    .sourceTable("OFFENDER_ADDRESS")
                    .status("INPROGRESS")
                    .build()));

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nextUpdate")
                    .then()
                    .statusCode(200)
                    .body("action", equalTo("INSERT"))
                    .body("dateChanged", notNullValue())
                    .body("offenderDeltaId", equalTo(1))
                    .body("offenderId", equalTo(99))
                    .body("sourceRecordId", equalTo(101))
                    .body("sourceTable", equalTo("OFFENDER_ADDRESS"))
                    .body("status", equalTo("INPROGRESS"))
            ;

        }

        @Test
        @DisplayName("Will return 404 when no new updates present")
        void willReturnNotFoundWhenNoNewUpdates() {
            when(offenderUpdatesService.getNextUpdate()).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nextUpdate")
                    .then()
                    .statusCode(404)
            ;

        }

        @Test
        @DisplayName("Will return conflict when unable to retrieve an update due to competing threads")
        void willConflictWhenUnableToRetrieveNextUpdate() {
            when(offenderUpdatesService.getNextUpdate()).thenThrow(new OffenderDeltaLockedException());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/secure/offenders/nextUpdate")
                    .then()
                    .statusCode(409)
            ;
        }
    }

}
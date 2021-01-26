package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.CustodyRelatedKeyDates;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;

class CustodyKeyDatesControllerTest {

    private final OffenderService offenderService = mock(OffenderService.class);
    private final ConvictionService convictionService = mock(ConvictionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .registerModules(new Jdk8Module(), new JavaTimeModule());


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
            new CustodyKeyDatesController(offenderService, convictionService, new FeatureSwitches()),
            new SecureControllerAdvice()
        );

    }

    @Nested
    class ReplaceAllCustodyKeyDateByNomsNumberAndBookingNumber {
        @BeforeEach
        void setUp() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(offenderService.offenderIdOfNomsNumber(any())).thenReturn(Optional.of(99L));
            when(convictionService.getAllActiveCustodialEventsWithBookingNumber(any(), any())).thenReturn(List.of(aCustodyEvent(88L, LocalDate
                .now())));
            when(convictionService.addOrReplaceOrDeleteCustodyKeyDates(any(), any(), any())).thenReturn(Custody
                .builder()
                .sentenceStartDate(LocalDate.now())
                .build());
        }

        @Test
        void WillReturn404WhenOffenderNotFound() {
            when(offenderService.offenderIdOfNomsNumber(any())).thenReturn(Optional.empty());

            given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(ReplaceCustodyKeyDates.builder().build()))
                .when()
                .post("/secure/offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", "G9542VP", "44463B")
                .then()
                .statusCode(404)
                .body("developerMessage", containsString("Offender with NOMS number G9542VP not found"));

            verify(offenderService).offenderIdOfNomsNumber("G9542VP");
        }

        @Test
        void WillReturn404WhenConvictionForBookingNotFound() {
            when(offenderService.offenderIdOfNomsNumber(any())).thenReturn(Optional.of(99L));
            when(convictionService.getAllActiveCustodialEventsWithBookingNumber(any(), any())).thenReturn(List.of());

            given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(ReplaceCustodyKeyDates.builder().build()))
                .when()
                .post("/secure/offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", "G9542VP", "44463B")
                .then()
                .statusCode(404)
                .body("developerMessage", containsString("Conviction with bookingNumber 44463B not found for offender with NOMS number G9542VP"));

            verify(convictionService).getAllActiveCustodialEventsWithBookingNumber(99L, "44463B");
        }

        @Test
        void WillReturn200WhenUpdateSucceeds() {
            when(convictionService.addOrReplaceOrDeleteCustodyKeyDates(any(), any(), any())).thenReturn(Custody
                .builder()
                .keyDates(CustodyRelatedKeyDates
                    .builder()
                    .build())
                .build());

            given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(ReplaceCustodyKeyDates.builder().build()))
                .when()
                .post("/secure/offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", "G9542VP", "44463B")
                .then()
                .statusCode(200);
        }

        private String json(ReplaceCustodyKeyDates custodyKeyDates) {
            try {
                return objectMapper.writeValueAsString(custodyKeyDates);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        @Nested
        class MultipleUpdateFeatureTurnedOn {
            @BeforeEach
            void setUp() {
                final var featureSwitches = new FeatureSwitches();
                featureSwitches.getNoms().getUpdate().getMultipleEvents().setUpdateBulkKeyDates(true);
                RestAssuredMockMvc.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
                RestAssuredMockMvc.standaloneSetup(
                    new CustodyKeyDatesController(offenderService, convictionService, featureSwitches ),
                    new SecureControllerAdvice()
                );

            }


            @Test
            void WillUpdateAllActiveCustodialEvents() {
                when(convictionService.getAllActiveCustodialEventsWithBookingNumber(any(), any())).thenReturn(List.of(aCustodyEvent(88L, LocalDate
                    .now()
                    .minusMonths(2)), aCustodyEvent(89L, LocalDate.now())));

                given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(json(ReplaceCustodyKeyDates.builder().build()))
                    .when()
                    .post("/secure/offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", "G9542VP", "44463B")
                    .then()
                    .statusCode(200);

                verify(convictionService).addOrReplaceOrDeleteCustodyKeyDates(any(), eq(88L), any());
                verify(convictionService).addOrReplaceOrDeleteCustodyKeyDates(any(), eq(89L), any());
            }

            @Test
            void WillReturnLatestSentence() {
                when(convictionService.addOrReplaceOrDeleteCustodyKeyDates(any(), eq(88L), any())).thenReturn(Custody
                    .builder()
                    .sentenceStartDate(LocalDate.parse("2020-05-22"))
                    .build());
                when(convictionService.addOrReplaceOrDeleteCustodyKeyDates(any(), eq(89L), any())).thenReturn(Custody
                    .builder()
                    .sentenceStartDate(LocalDate.parse("2021-01-13"))
                    .build());

                when(convictionService.getAllActiveCustodialEventsWithBookingNumber(any(), any())).thenReturn(List.of(aCustodyEvent(88L, LocalDate
                    .parse("2020-05-22")), aCustodyEvent(89L, LocalDate.parse("2021-01-13"))));

                given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(json(ReplaceCustodyKeyDates.builder().build()))
                    .when()
                    .post("/secure/offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", "G9542VP", "44463B")
                    .then()
                    .statusCode(200)
                    .body("sentenceStartDate", CoreMatchers.equalTo("2021-01-13"));
            }
        }
        @Nested
        class MultipleUpdateFeatureTurnedOff {
            @BeforeEach
            void setUp() {
                final var featureSwitches = new FeatureSwitches();
                featureSwitches.getNoms().getUpdate().getMultipleEvents().setUpdateBulkKeyDates(false);
                RestAssuredMockMvc.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
                RestAssuredMockMvc.standaloneSetup(
                    new CustodyKeyDatesController(offenderService, convictionService, featureSwitches ),
                    new SecureControllerAdvice()
                );

            }


            @Test
            void WillReturn404WithMultipleCustodialEvents() {
                when(convictionService.getAllActiveCustodialEventsWithBookingNumber(any(), any())).thenReturn(List.of(aCustodyEvent(88L, LocalDate
                    .now()
                    .minusMonths(2)), aCustodyEvent(89L, LocalDate.now())));

                given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(json(ReplaceCustodyKeyDates.builder().build()))
                    .when()
                    .post("/secure/offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", "G9542VP", "44463B")
                    .then()
                    .statusCode(404);

            }

        }

    }

}
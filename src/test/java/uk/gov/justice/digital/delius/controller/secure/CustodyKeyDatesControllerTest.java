package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.CustodyRelatedKeyDates;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class CustodyKeyDatesControllerTest {

    private OffenderService offenderService = mock(OffenderService.class);
    private ConvictionService convictionService = mock(ConvictionService.class);
    private ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .registerModules(new Jdk8Module(), new JavaTimeModule());


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.config =  newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
                new CustodyKeyDatesController(offenderService, convictionService),
                new SecureControllerAdvice()
        );

    }

    @Nested
    class ReplaceAllCustodyKeyDateByNomsNumberAndBookingNumber {
        @BeforeEach
        void setUp() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
            when(offenderService.offenderIdOfNomsNumber(any())).thenReturn(Optional.of(99L));
            when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(any(), any())).thenReturn(Optional.of(88L));
            when(convictionService.addOrReplaceOrDeleteCustodyKeyDates(any(), any(), any())).thenReturn(Custody.builder().build());
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
        void WillReturn404WhenConvictionForBookingNotFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
            when(offenderService.offenderIdOfNomsNumber(any())).thenReturn(Optional.of(99L));
            when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(any(), any())).thenReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(json(ReplaceCustodyKeyDates.builder().build()))
                    .when()
                    .post("/secure/offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", "G9542VP", "44463B")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("Conviction with bookingNumber 44463B not found for offender with NOMS number G9542VP"));

            verify(convictionService).getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B");
        }

        @Test
        void WillReturn404WhenASingleConvictionForBookingNotFoundButHasDuplicates() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
            when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(any(), any())).thenThrow(new ConvictionService.DuplicateConvictionsForBookingNumberException(2));

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(json(ReplaceCustodyKeyDates.builder().build()))
                    .when()
                    .post("/secure/offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", "G9542VP", "44463B")
                    .then()
                    .statusCode(404)
                    .body("developerMessage", containsString("Single active conviction for G9542VP with booking number 44463B not found. Instead has 2 convictions"));
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

        private String json(ReplaceCustodyKeyDates custodyKeyDates)  {
            try {
                return objectMapper.writeValueAsString(custodyKeyDates);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


    }

}
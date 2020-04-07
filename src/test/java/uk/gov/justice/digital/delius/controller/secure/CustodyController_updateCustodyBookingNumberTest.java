package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.UpdateCustodyBookingNumber;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.OffenderIdentifierService;

import java.time.LocalDate;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustodyController_updateCustodyBookingNumberTest {

    private CustodyService custodyService = mock(CustodyService.class);
    private OffenderIdentifierService offenderIdentifierService = mock(OffenderIdentifierService.class);
    private ArgumentCaptor<UpdateCustodyBookingNumber> updateCustodyArgumentCaptor = ArgumentCaptor.forClass(UpdateCustodyBookingNumber.class);

    private ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .registerModules(new Jdk8Module(), new JavaTimeModule());

    @Before
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new CustodyResource(custodyService, offenderIdentifierService),
                new SecureControllerAdvice()
        );

        when(custodyService.updateCustodyBookingNumber(anyString(), any())).thenReturn(Custody.builder().build());
    }

    @Test
    public void requestMissingBookingNumber_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateCustodyBookingNumberOf(null, LocalDate.now())))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/custody/bookingNumber", "G9542VP"))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("Missing a book number"));
    }

    @Test
    public void requestMissingSentenceStartDate_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateCustodyBookingNumberOf("44463B", null)))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/custody/bookingNumber", "G9542VP"))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("Missing a sentence start date"));
    }

    @Test
    public void requestAllPresent_returnsOK() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateCustodyBookingNumberOf("44463B", LocalDate.now())))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/custody/bookingNumber", "G9542VP"))
                .then()
                .statusCode(200);
    }

    @Test
    public void requestReturnsUpdatedCustody() throws JsonProcessingException {
        when(custodyService.updateCustodyBookingNumber(anyString(), any())).thenReturn(Custody
                .builder()
                .bookingNumber("44463B")
                .build());

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateCustodyBookingNumberOf("44463B", LocalDate.now())))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/custody/bookingNumber", "G9542VP"))
                .then()
                .statusCode(200)
                .body("bookingNumber", equalTo("44463B"));
    }

    @Test
    public void requestWillCallUpdateCustodyBookingNumberService() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateCustodyBookingNumberOf("44463B", LocalDate.of(2020, 2, 19))))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/custody/bookingNumber", "G9542VP"))
                .then()
                .statusCode(200);


        verify(custodyService).updateCustodyBookingNumber(eq("G9542VP"), updateCustodyArgumentCaptor.capture());
        assertThat(updateCustodyArgumentCaptor.getValue().getBookingNumber()).isEqualTo("44463B");
        assertThat(updateCustodyArgumentCaptor.getValue().getSentenceStartDate()).isEqualTo(LocalDate.of(2020, 2, 19));
    }

    private UpdateCustodyBookingNumber createUpdateCustodyBookingNumberOf(String bookingNumber, LocalDate sentenceStartDate) {
        return UpdateCustodyBookingNumber
                .builder()
                .bookingNumber(bookingNumber)
                .sentenceStartDate(sentenceStartDate)
                .build();
    }

    private String json(UpdateCustodyBookingNumber custody) throws JsonProcessingException {
        return objectMapper.writeValueAsString(custody);
    }


}

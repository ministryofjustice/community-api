package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.OffenderIdentifierService;

import java.util.List;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustodyController_replaceNomsNumberTest {

    private CustodyService custodyService = mock(CustodyService.class);
    private OffenderIdentifierService offenderIdentifierService = mock(OffenderIdentifierService.class);
    private ArgumentCaptor<UpdateOffenderNomsNumber> updateOffenderNomsNumberArgumentCaptor = ArgumentCaptor
            .forClass(UpdateOffenderNomsNumber.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        RestAssuredMockMvc.config =  newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
                new CustodyResource(custodyService, offenderIdentifierService),
                new SecureControllerAdvice()
        );

        when(offenderIdentifierService.replaceNomsNumber(anyString(), any()))
                .thenReturn(List.of(IDs.builder().build()));
    }

    @Test
    public void requestMissingNomsNumber_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateOffenderNomsNumberOf(null)))
                .when()
                .put("/secure/offenders/nomsNumber/G3333AA/nomsNumber")
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("Missing a NOMS number"));
    }

    @Test
    public void requestBlankNomsNumber_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateOffenderNomsNumberOf("")))
                .when()
                .put("/secure/offenders/nomsNumber/G3333AA/nomsNumber")
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("Missing a NOMS number"));
    }

    @Test
    public void conflictingRequestExceptionDueToNAMSNumberAlreadyAssigned_returnsConflict() throws JsonProcessingException {
        when(offenderIdentifierService.replaceNomsNumber(anyString(), any()))
                .thenThrow(new ConflictingRequestException("NOMS number G5555TT is already assigned to X88888"));

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateOffenderNomsNumberOf("G5555TT")))
                .when()
                .put("/secure/offenders/nomsNumber/G3333AA/nomsNumber")
                .then()
                .statusCode(409)
                .body("developerMessage", containsString("NOMS number G5555TT is already assigned to X88888"));
    }

    @Test
    public void requestAllPresent_returnsOK() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateOffenderNomsNumberOf("G5555TT")))
                .when()
                .put("/secure/offenders/nomsNumber/G3333AA/nomsNumber")
                .then()
                .statusCode(200);
    }

    @Test
    public void requestReturnsAllUpdatedOffenderIDs() throws JsonProcessingException {
        when(offenderIdentifierService.replaceNomsNumber(anyString(), any())).thenReturn(List.of(
                IDs
                        .builder()
                        .nomsNumber("G5555TT")
                        .crn("X12345")
                        .build(),
                IDs
                        .builder()
                        .nomsNumber("G5555TT")
                        .crn("X92382")
                        .build()));

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateOffenderNomsNumberOf("G5555TT")))
                .when()
                .put("/secure/offenders/nomsNumber/G3333AA/nomsNumber")
                .then()
                .statusCode(200)
                .body("[0].nomsNumber", equalTo("G5555TT"))
                .body("[0].crn", equalTo("X12345"))
                .body("[1].nomsNumber", equalTo("G5555TT"))
                .body("[1].crn", equalTo("X92382"))
                ;
    }

    @Test
    public void requestWillCallUpdateCustodyService() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateOffenderNomsNumberOf("G5555TT")))
                .when()
                .put("/secure/offenders/nomsNumber/G3333AA/nomsNumber")
                .then()
                .statusCode(200);


        verify(offenderIdentifierService)
                .replaceNomsNumber(eq("G3333AA"), updateOffenderNomsNumberArgumentCaptor.capture());
        assertThat(updateOffenderNomsNumberArgumentCaptor.getValue().getNomsNumber()).isEqualTo("G5555TT");
    }

    private UpdateOffenderNomsNumber createUpdateOffenderNomsNumberOf(String nomsNumber) {
        return UpdateOffenderNomsNumber
                .builder()
                .nomsNumber(nomsNumber)
                .build();
    }

    private String json(UpdateOffenderNomsNumber custody) throws JsonProcessingException {
        return objectMapper.writeValueAsString(custody);
    }

}

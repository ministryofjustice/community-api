package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.Institution;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.OffenderIdentifierService;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustodyController_updateCustodyTest {

    private CustodyService custodyService = mock(CustodyService.class);
    private OffenderIdentifierService offenderIdentifierService = mock(OffenderIdentifierService.class);
    private ArgumentCaptor<UpdateCustody> updateCustodyArgumentCaptor = ArgumentCaptor.forClass(UpdateCustody.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        RestAssuredMockMvc.config =  newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
                new CustodyResource(custodyService, offenderIdentifierService),
                new SecureControllerAdvice()
        );

        when(custodyService.updateCustodyPrisonLocation(anyString(), anyString(), any())).thenReturn(Custody.builder().build());
    }

    @Test
    public void requestMissingPrisonCode_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateCustodyManagerOf(null)))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/custody/bookingNumber/%s", "G9542VP", "44463B"))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("NOMS prison institution code"));
    }

    @Test
    public void requestAllPresent_returnsOK() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateCustodyManagerOf("MDI")))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/custody/bookingNumber/%s", "G9542VP", "44463B"))
                .then()
                .statusCode(200);
    }

    @Test
    public void requestReturnsUpdatedCustody() throws JsonProcessingException {
        when(custodyService.updateCustodyPrisonLocation(anyString(), anyString(), any())).thenReturn(Custody
                .builder()
                .institution(Institution
                        .builder()
                        .nomsPrisonInstitutionCode("MDI")
                        .build())
                .bookingNumber("1234")
                .build());

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateCustodyManagerOf("MDI")))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/custody/bookingNumber/%s", "G9542VP", "44463B"))
                .then()
                .statusCode(200)
                .body("bookingNumber", equalTo("1234"))
                .body("institution.nomsPrisonInstitutionCode", equalTo("MDI"));
    }

    @Test
    public void requestWillCallUpdateCustodyService() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(json(createUpdateCustodyManagerOf("MDI")))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/custody/bookingNumber/%s", "G9542VP", "44463B"))
                .then()
                .statusCode(200);


        verify(custodyService).updateCustodyPrisonLocation(eq("G9542VP"), eq("44463B"), updateCustodyArgumentCaptor.capture());
        assertThat(updateCustodyArgumentCaptor.getValue().getNomsPrisonInstitutionCode()).isEqualTo("MDI");
    }

    private UpdateCustody createUpdateCustodyManagerOf(String prisonCode) {
        return UpdateCustody
                .builder()
                .nomsPrisonInstitutionCode(prisonCode)
                .build();
    }

    private String json(UpdateCustody custody) throws JsonProcessingException {
        return objectMapper.writeValueAsString(custody);
    }


}

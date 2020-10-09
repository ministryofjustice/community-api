package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficerSwitch;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.service.AlfrescoService;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.DocumentService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderManagerService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.SentenceService;
import uk.gov.justice.digital.delius.service.UserService;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
public class OffendersResource_SwitchResponsibleOfficerAPITest {

    private static final String SOME_OFFENDER_NOMS_NUMBER = "G9542VP";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private OffenderService offenderService;
    @Mock
    private AlfrescoService alfrescoService;
    @Mock
    private DocumentService documentService;
    @Mock
    private ContactService contactService;
    @Mock
    private ConvictionService convictionService;
    @Mock
    private OffenderManagerService offenderManagerService;
    @Mock
    private NsiService nsiService;
    @Mock
    private SentenceService sentenceService;
    @Mock
    private UserService userService;
    @Mock
    private CurrentUserSupplier currentUserSupplier;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
                new OffendersResource(offenderService, alfrescoService, documentService, contactService, convictionService, nsiService, offenderManagerService, sentenceService, userService, currentUserSupplier),
                new SecureControllerAdvice()
        );
    }

    @Test
    public void requestMissingASingleTrueOption_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(objectMapper
                        .writeValueAsString(ResponsibleOfficerSwitch
                                .builder()
                                .switchToCommunityOffenderManager(true)
                                .switchToPrisonOffenderManager(true)
                                .build()))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/responsibleOfficer/switch", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("Either set true for the prisoner offender manager or the community offender manager"));

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(objectMapper
                        .writeValueAsString(ResponsibleOfficerSwitch
                                .builder()
                                .switchToCommunityOffenderManager(false)
                                .switchToPrisonOffenderManager(false)
                                .build()))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/responsibleOfficer/switch", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("Either set true for the prisoner offender manager or the community offender manager"));
    }
    @Test
    public void requestReturnsOKWhenRequestSucceeds() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(objectMapper
                        .writeValueAsString(ResponsibleOfficerSwitch
                                .builder()
                                .switchToCommunityOffenderManager(false)
                                .switchToPrisonOffenderManager(true)
                                .build()))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/responsibleOfficer/switch", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(200);
    }
}

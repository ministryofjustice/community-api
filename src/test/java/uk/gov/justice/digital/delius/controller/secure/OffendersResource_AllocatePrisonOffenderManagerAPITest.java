package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.InvalidRequestException;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.service.AlfrescoService;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.DocumentService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderManagerService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.SentenceService;
import uk.gov.justice.digital.delius.service.UserService;

import java.util.Optional;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
public class OffendersResource_AllocatePrisonOffenderManagerAPITest {

    private static final String SOME_OFFENDER_NOMS_NUMBER = "G9542VP";
    private static final Long SOME_STAFF_ID = 12345L;
    private static final String SOME_OFFICER_FORENAMES = "John";
    private static final String SOME_OFFICER_SURNAME = "Smith";
    private static final String SOME_PRISON_NOMS_CODE = "BWI";

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
    @Mock
    private CustodyService custodyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
                new OffendersResource(offenderService, alfrescoService, documentService, contactService, convictionService, nsiService, offenderManagerService, sentenceService, userService, currentUserSupplier, custodyService),
                new SecureControllerAdvice()
        );
    }

    @Test
    public void requestMissingPrisonCode_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_STAFF_ID, null, null, null))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("NOMS prison institution code"));
    }

    @Test
    public void requestWithEmptyPrisonCode_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_STAFF_ID, null, null, ""))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("NOMS prison institution code"));
    }

    @Test
    public void requestMissingStaffIdAndNames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(null, null, null, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("either officer or staff id"));
    }

    @Test
    public void requestWithEmptyStaffIdAndNames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(null, "", "", SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("both officer names"));
    }

    @Test
    public void requestWithBothStaffIdAndNames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_STAFF_ID, SOME_OFFICER_FORENAMES, SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("either officer OR staff id"));
    }

    @Test
    public void requestMissingOfficerNames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonMissingBothNames())
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("both officer names"));
    }

    @Test
    public void requestWithEmptyOfficerNames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(null, "", "", SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("both officer names"));
    }

    @Test
    public void requestMissingOfficerForenames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(null, null, SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("forenames"));
    }

    @Test
    public void requestWithEmptyOfficerForenames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(null, "", SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("forenames"));
    }

    @Test
    public void requestMissingOfficerSurname_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(null, SOME_OFFICER_FORENAMES, null, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("surname"));
    }

    @Test
    public void requestWithEmptyOfficerSurname_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(null, SOME_OFFICER_FORENAMES, "", SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("surname"));
    }

    @Test
    public void requestWithStaffId_offenderManagerNotFoundOrCreated_returnsNotFound() throws JsonProcessingException {
        given(offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                SOME_OFFENDER_NOMS_NUMBER,
                SOME_STAFF_ID,
                createPrisonOffenderManagerOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
        )
                .willReturn(Optional.empty());

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(404);
    }

    @Test
    public void requestWithOfficerName_offenderManagerNotFoundOrCreated_returnsNotFound() throws JsonProcessingException {
        given(offenderManagerService.allocatePrisonOffenderManagerByName(
                SOME_OFFENDER_NOMS_NUMBER,
                createPrisonOffenderManagerOf(null, SOME_OFFICER_FORENAMES, SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
        )
                .willReturn(Optional.empty());

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(null, SOME_OFFICER_FORENAMES, SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(404);
    }

    @Test
    public void staffNotInExpectedProbationArea_returnsBadRequest() throws JsonProcessingException {
        given(offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                SOME_OFFENDER_NOMS_NUMBER,
                SOME_STAFF_ID,
                createPrisonOffenderManagerOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
        )
                .willThrow(InvalidRequestException.class);

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400);
    }

    @Test
    public void requestWithPrisonNomsCodeNotFound_returnsBadRequest() throws JsonProcessingException {
        given(offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                SOME_OFFENDER_NOMS_NUMBER,
                SOME_STAFF_ID,
                createPrisonOffenderManagerOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
        )
                .willThrow(new InvalidRequestException(String.format("Prison NOMS code %s not found", SOME_PRISON_NOMS_CODE)));

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400);
    }

    @Nested
    class PrisonLocationUpdate {
        @Test
        public void requestWithStaffId_willRequestUpdateToPrisonLocation() throws JsonProcessingException {
            given(offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    SOME_OFFENDER_NOMS_NUMBER,
                    SOME_STAFF_ID,
                    createPrisonOffenderManagerOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
            )
                    .willReturn(Optional.of(CommunityOrPrisonOffenderManager.builder().build()));

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(createPrisonOffenderManagerJsonOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
                    .when()
                    .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                    .then()
                    .statusCode(200);
            verify(custodyService).updateCustodyPrisonLocation(SOME_OFFENDER_NOMS_NUMBER, SOME_PRISON_NOMS_CODE);
        }

        @Test
        public void requestWithOfficerName_willRequestUpdateToPrisonLocation() throws JsonProcessingException {
            given(offenderManagerService.allocatePrisonOffenderManagerByName(
                    SOME_OFFENDER_NOMS_NUMBER,
                    createPrisonOffenderManagerOf(null, SOME_OFFICER_FORENAMES, SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
            )
                    .willReturn(Optional.of(CommunityOrPrisonOffenderManager.builder().build()));

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(createPrisonOffenderManagerJsonOf(null, SOME_OFFICER_FORENAMES, SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
                    .when()
                    .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                    .then()
                    .statusCode(200);

            verify(custodyService).updateCustodyPrisonLocation(SOME_OFFENDER_NOMS_NUMBER, SOME_PRISON_NOMS_CODE);
        }
        @Test
        public void requestWithOfficerName_offenderManagerNotFoundOrCreated_willNotRequestUpdateToPrisonLocation() throws JsonProcessingException {
            given(offenderManagerService.allocatePrisonOffenderManagerByName(
                    SOME_OFFENDER_NOMS_NUMBER,
                    createPrisonOffenderManagerOf(null, SOME_OFFICER_FORENAMES, SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
            )
                    .willReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(createPrisonOffenderManagerJsonOf(null, SOME_OFFICER_FORENAMES, SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
                    .when()
                    .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                    .then()
                    .statusCode(404);

            verify(custodyService, never()).updateCustodyPrisonLocation(any(), any());
        }

        @Test
        public void requestWithStaffId_offenderManagerNotFoundOrCreated_willNotRequestUpdateToPrisonLocation() throws JsonProcessingException {
            given(offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    SOME_OFFENDER_NOMS_NUMBER,
                    SOME_STAFF_ID,
                    createPrisonOffenderManagerOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
            )
                    .willReturn(Optional.empty());

            given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(createPrisonOffenderManagerJsonOf(SOME_STAFF_ID, null, null, SOME_PRISON_NOMS_CODE))
                    .when()
                    .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                    .then()
                    .statusCode(404);

            verify(custodyService, never()).updateCustodyPrisonLocation(any(), any());
        }


    }


    private CreatePrisonOffenderManager createPrisonOffenderManagerOf(final Long staffId, final String officerForenames, final String officerSurname, final String prisonCode) {
        Human officer = null;
        if (officerForenames != null || officerSurname != null) {
            officer = Human.builder().forenames(officerForenames).surname(officerSurname).build();
        }
        return CreatePrisonOffenderManager
                .builder()
                .staffId(staffId)
                .officer(officer)
                .nomsPrisonInstitutionCode(prisonCode)
                .build();
    }

    private String createPrisonOffenderManagerJsonOf(final Long staffId, final String officerForenames, final String officerSurname, final String prisonCode) throws JsonProcessingException {
        return objectMapper.writeValueAsString(createPrisonOffenderManagerOf(staffId, officerForenames, officerSurname, prisonCode));
    }

    private String createPrisonOffenderManagerJsonMissingBothNames() throws JsonProcessingException {
        return objectMapper.writeValueAsString(
                CreatePrisonOffenderManager
                        .builder()
                        .officer(Human.builder().build())
                        .nomsPrisonInstitutionCode(SOME_PRISON_NOMS_CODE)
                        .build()
        );
    }

}

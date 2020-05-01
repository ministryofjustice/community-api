package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.digital.delius.controller.InvalidRequestException;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.controller.secure.OffendersResource;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Human;
import uk.gov.justice.digital.delius.service.*;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_AllocatePrisonOffenderManagerAPITest {

    private static final String SOME_OFFENDER_NOMS_NUMBER = "G9542VP";
    private static final String SOME_OFFICER_CODE = "BWIA010";
    private static final String SOME_OFFICER_FORENAMES = "John";
    private static final String SOME_OFFICER_SURNAME = "Smith";
    private static final String SOME_PRISON_NOMS_CODE = "BWI";

    private OffenderService offenderService = mock(OffenderService.class);
    private AlfrescoService alfrescoService = mock(AlfrescoService.class);
    private DocumentService documentService = mock(DocumentService.class);
    private ContactService contactService = mock(ContactService.class);
    private ConvictionService convictionService = mock(ConvictionService.class);
    private OffenderManagerService offenderManagerService = mock(OffenderManagerService.class);
    private NsiService nsiService = mock(NsiService.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new OffendersResource(offenderService, alfrescoService, documentService, contactService, convictionService, nsiService, offenderManagerService),
                new SecureControllerAdvice()
        );
    }

    @Test
    public void requestMissingPrisonCode_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_OFFICER_CODE, null, null, null))
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
                .body(createPrisonOffenderManagerJsonOf(SOME_OFFICER_CODE, null, null, ""))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("NOMS prison institution code"));
    }

    @Test
    public void requestMissingOfficerCodeAndNames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(null, null, null, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("either officer or officer code"));
    }

    @Test
    public void requestWithEmptyOfficerCodeAndNames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf("", "", "", SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("both officer names"));
    }

    @Test
    public void requestWithBothOfficerCodeAndNames_returnsBadRequest() throws JsonProcessingException {
        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_OFFICER_CODE, SOME_OFFICER_FORENAMES, SOME_OFFICER_SURNAME, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400)
                .body("developerMessage", containsString("either officer OR officer code"));
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
    public void requestWithOfficerCode_offenderManagerNotFoundOrCreated_returnsNotFound() throws JsonProcessingException {
        given(offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                SOME_OFFENDER_NOMS_NUMBER,
                SOME_OFFICER_CODE,
                createPrisonOffenderManagerOf(SOME_OFFICER_CODE, null, null, SOME_PRISON_NOMS_CODE))
        )
                .willReturn(Optional.empty());

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_OFFICER_CODE, null, null, SOME_PRISON_NOMS_CODE))
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
        given(offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                SOME_OFFENDER_NOMS_NUMBER,
                SOME_OFFICER_CODE,
                createPrisonOffenderManagerOf(SOME_OFFICER_CODE, null, null, SOME_PRISON_NOMS_CODE))
        )
                .willThrow(InvalidRequestException.class);

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_OFFICER_CODE, null, null, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400);
    }

    @Test
    public void requestWithPrisonNomsCodeNotFound_returnsBadRequest() throws JsonProcessingException {
        given(offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                SOME_OFFENDER_NOMS_NUMBER,
                SOME_OFFICER_CODE,
                createPrisonOffenderManagerOf(SOME_OFFICER_CODE, null, null, SOME_PRISON_NOMS_CODE))
        )
                .willThrow(new InvalidRequestException(String.format("Prison NOMS code %s not found", SOME_PRISON_NOMS_CODE)));

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .body(createPrisonOffenderManagerJsonOf(SOME_OFFICER_CODE, null, null, SOME_PRISON_NOMS_CODE))
                .when()
                .put(String.format("/secure/offenders/nomsNumber/%s/prisonOffenderManager", SOME_OFFENDER_NOMS_NUMBER))
                .then()
                .statusCode(400);
    }


    private CreatePrisonOffenderManager createPrisonOffenderManagerOf(String officerCode, String officerForenames, String officerSurname, String prisonCode) {
        Human officer = null;
        if(officerForenames != null || officerSurname != null) {
            officer = Human.builder().forenames(officerForenames).surname(officerSurname).build();
        }
        return CreatePrisonOffenderManager
                .builder()
                .officerCode(officerCode)
                .officer(officer)
                .nomsPrisonInstitutionCode(prisonCode)
                .build();
    }

    private String createPrisonOffenderManagerJsonOf(String officerCode, String officerForenames, String officerSurname, String prisonCode) throws JsonProcessingException {
        return objectMapper.writeValueAsString(createPrisonOffenderManagerOf(officerCode, officerForenames, officerSurname, prisonCode));
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

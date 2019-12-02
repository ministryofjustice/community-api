package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.controller.CustodyNotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class OffendersResource_GetLatestRecallAndReleaseForOffender {

    private static final Long ANY_OFFENDER_ID = 123L;
    private static final Long SOME_OFFENDER_ID = 456L;
    private static final Integer SOME_ACTIVE_CUSTODY_CONVICTION_COUNT = 99;
    private static final Optional<Long> MAYBE_ANY_OFFENDER_ID = Optional.of(ANY_OFFENDER_ID);
    private static final Optional<Long> MAYBE_SOME_OFFENDER_ID = Optional.of(SOME_OFFENDER_ID);
    private static final Optional<Long> OFFENDER_ID_NOT_FOUND = Optional.empty();
    private static final Long SOME_CUSTODIAL_EVENT_ID = 333L;
    private static final uk.gov.justice.digital.delius.jpa.standard.entity.Event SOME_CUSTODIAL_EVENT
            = uk.gov.justice.digital.delius.jpa.standard.entity.Event.builder().eventId(SOME_CUSTODIAL_EVENT_ID).build();

    @LocalServerPort
    int port;

    @MockBean
    private OffenderService mockOffenderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.good}")
    private String validOauthToken;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void getLatestRecallAndReleaseForOffender_offenderFound_returnsOk() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfNomsNumber(anyString()))
                .willReturn(MAYBE_ANY_OFFENDER_ID);
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(anyLong()))
                .willReturn(OffenderLatestRecall.builder().build());
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/release")
                .then()
                .statusCode(200);

    }

    @Test
    public void getLatestRecallAndReleaseForOffender_offenderFound_recallDataOk() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfNomsNumber(anyString()))
                .willReturn(MAYBE_ANY_OFFENDER_ID);
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(getDefaultOffenderRecall())
                .lastRelease(getDefaultOffenderRelease())
                .build();
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(anyLong()))
                .willReturn(expectedOffenderRecall);

        final var offenderLatestRecall = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/release")
                .then()
                .extract()
                .body()
                .as(OffenderLatestRecall.class);

        assertThat(offenderLatestRecall).isEqualTo(expectedOffenderRecall);
    }

    @Test
    public void getLatestRecallAndReleaseForOffender_missingEstablishmentType_returnsNullEstablishmentType() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfNomsNumber(anyString()))
                .willReturn(MAYBE_ANY_OFFENDER_ID);
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(getOffenderRecallNotEstablishment())
                .lastRelease(getOffenderReleaseNotEstablishment())
                .build();
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(anyLong()))
                .willReturn(expectedOffenderRecall);

        final var offenderLatestRecall = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/release")
                .then()
                .extract()
                .body()
                .as(OffenderLatestRecall.class);

        assertThat(offenderLatestRecall.getLastRecall().getInstitution().getEstablishmentType()).isNull();
        assertThat(offenderLatestRecall.getLastRelease().getInstitution().getEstablishmentType()).isNull();
    }

    @Test
    public void getLatestRecallAndReleaseForOffender_offenderNotFound_returnsNotFound() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfNomsNumber(anyString()))
                .willReturn(OFFENDER_ID_NOT_FOUND);

        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/release")
                .then()
                .statusCode(404);
    }

    @Test
    public void getLatestRecallAndReleaseForOffenderByCrn_offenderFound_returnsOk() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfCrn(anyString()))
                .willReturn(MAYBE_ANY_OFFENDER_ID);
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(anyLong()))
                .willReturn(OffenderLatestRecall.builder().build());
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/release")
                .then()
                .statusCode(200);

    }

    @Test
    public void getLatestRecallAndReleaseForOffenderByCrn_offenderFound_recallDataOk() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfCrn(anyString()))
                .willReturn(MAYBE_ANY_OFFENDER_ID);
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(getDefaultOffenderRecall())
                .lastRelease(getDefaultOffenderRelease())
                .build();
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(anyLong()))
                .willReturn(expectedOffenderRecall);

        final var offenderLatestRecall = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/release")
                .then()
                .extract()
                .body()
                .as(OffenderLatestRecall.class);

        assertThat(offenderLatestRecall).isEqualTo(expectedOffenderRecall);
    }

    @Test
    public void getLatestRecallAndReleaseForOffenderByCrn_offenderNotFound_returnsNotFound() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfCrn(anyString()))
                .willReturn(OFFENDER_ID_NOT_FOUND);
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/release")
                .then()
                .statusCode(404);

    }

    @Test
    public void getLatestRecallAndReleaseForOffender_notSingleCustodyEvent_returnsBadRequest() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfCrn(anyString()))
                .willReturn(MAYBE_ANY_OFFENDER_ID);
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(ANY_OFFENDER_ID))
                .willThrow(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class);
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/release")
                .then()
                .statusCode(400);

    }

    @Test
    public void getLatestRecallAndReleaseForOffender_notSingleCustodyEvent_returnsDetailsInErrorMessage() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfCrn(anyString()))
                .willReturn(MAYBE_SOME_OFFENDER_ID);
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(SOME_OFFENDER_ID))
                .willThrow(new ConvictionService.SingleActiveCustodyConvictionNotFoundException(SOME_OFFENDER_ID, SOME_ACTIVE_CUSTODY_CONVICTION_COUNT));

        ErrorResponse errorResponse = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/release")
                .then()
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(errorResponse.getDeveloperMessage()).contains(SOME_OFFENDER_ID.toString());
        assertThat(errorResponse.getDeveloperMessage()).contains(SOME_ACTIVE_CUSTODY_CONVICTION_COUNT.toString());

    }

    @Test
    public void getLatestRecallAndReleaseForOffender_custodyRecordNotFound_returnsBadRequest() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfCrn(anyString()))
                .willReturn(MAYBE_ANY_OFFENDER_ID);
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(ANY_OFFENDER_ID))
                .willThrow(CustodyNotFoundException.class);
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/release")
                .then()
                .statusCode(400);
    }

    @Test
    public void getLatestRecallAndReleaseForOffender_custodyRecordNotFound_returnsEventIdInErrorMessage() {
        org.mockito.BDDMockito.given(mockOffenderService.offenderIdOfCrn(anyString()))
                .willReturn(MAYBE_ANY_OFFENDER_ID);
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(ANY_OFFENDER_ID))
                .willThrow(new CustodyNotFoundException(SOME_CUSTODIAL_EVENT));
        ErrorResponse errorResponse = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/release")
                .then()
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(errorResponse.getDeveloperMessage()).contains(SOME_CUSTODIAL_EVENT_ID.toString());
    }



    private OffenderRecall getDefaultOffenderRecall() {
        return offenderRecallBuilder().build();
    }

    private OffenderRelease getDefaultOffenderRelease() {
        return offenderReleaseBuilder().build();
    }

    private OffenderRecall getOffenderRecallNotEstablishment() {
        final var nonEstablishmentInstitution = recallInstitutionBuilder().isEstablishment(false).establishmentType(null).build();
        return offenderRecallBuilder().institution(nonEstablishmentInstitution).build();
    }

    private OffenderRelease getOffenderReleaseNotEstablishment() {
        final var nonEstablishmentInstitution = releaseInstitutionBuilder().isEstablishment(false).establishmentType(null).build();
        return offenderReleaseBuilder().institution(nonEstablishmentInstitution).build();
    }

    private OffenderRecall.OffenderRecallBuilder offenderRecallBuilder() {
        Institution recallInstitution = recallInstitutionBuilder().build();
        return OffenderRecall.builder()
                .date(LocalDate.of(2019, 11, 27))
                .reason(KeyValue.builder().code("TEST_RECALL_REASON_CODE").description("Test recall reason description").build())
                .notes("Test recall notes")
                .institution(recallInstitution);
    }

    private Institution.InstitutionBuilder recallInstitutionBuilder() {
        return Institution.builder()
                .code("RECALL_INSTITUTION_CODE")
                .description("Recall institution description")
                .institutionId(1234L)
                .institutionName("Recall institution")
                .isEstablishment(true)
                .isPrivate(true)
                .establishmentType(KeyValue.builder().code("RECALL_ESTABLISHMENT_TYPE_CODE").description("Recall establishment type description").build());
    }

    private OffenderRelease.OffenderReleaseBuilder offenderReleaseBuilder() {
        Institution releaseInstitution = releaseInstitutionBuilder().build();
        return OffenderRelease.builder()
                .date(LocalDate.of(2019, 11, 26))
                .reason(KeyValue.builder().code("TEST_RELEASE_REASON_CODE").description("Test release reason description").build())
                .notes("Test release notes")
                .institution(releaseInstitution);
    }

    private Institution.InstitutionBuilder releaseInstitutionBuilder() {
        return Institution.builder()
                .code("RELEASE_INSTITUTION_CODE")
                .description("Release institution description")
                .institutionId(5678L)
                .institutionName("Release institution")
                .isEstablishment(true)
                .isPrivate(true)
                .establishmentType(KeyValue.builder().code("RELEASE_ESTABLISHMENT_TYPE_CODE").description("Release establishment type description").build());
    }

}

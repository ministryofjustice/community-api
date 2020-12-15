package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.CustodyNotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.Institution;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRelease;
import uk.gov.justice.digital.delius.helpers.CurrentUserSupplier;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.NsiService;
import uk.gov.justice.digital.delius.service.OffenderManagerService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.SentenceService;
import uk.gov.justice.digital.delius.service.UserAccessService;
import uk.gov.justice.digital.delius.service.UserService;

import java.time.LocalDate;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_GetLatestRecallAndReleaseForOffenderAPITest {

    private static final Long SOME_OFFENDER_ID = 456L;
    private static final String SOME_NOMS_NUMBER = "G9542VP";
    private static final String SOME_CRN_NUMBER = "X320741";
    private static final Integer SOME_ACTIVE_CUSTODY_CONVICTION_COUNT = 99;
    private static final Optional<Long> MAYBE_SOME_OFFENDER_ID = Optional.of(SOME_OFFENDER_ID);
    private static final Optional<Long> OFFENDER_ID_NOT_FOUND = Optional.empty();
    private static final Long SOME_CUSTODIAL_EVENT_ID = 333L;
    private static final Event SOME_CUSTODIAL_EVENT = Event.builder().eventId(SOME_CUSTODIAL_EVENT_ID).build();

    private final OffenderService offenderService = mock(OffenderService.class);
    private final ContactService contactService = mock(ContactService.class);
    private final ConvictionService convictionService = mock(ConvictionService.class);
    private final OffenderManagerService offenderManagerService = mock(OffenderManagerService.class);
    private final NsiService nsiService = mock(NsiService.class);
    private final SentenceService sentenceService = mock(SentenceService.class);
    private final UserService userService = mock(UserService.class);
    private final CurrentUserSupplier currentUserSupplier = mock(CurrentUserSupplier.class);
    private final CustodyService custodyService = mock(CustodyService.class);
    private final UserAccessService userAccessService = mock(UserAccessService.class);

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
                new OffendersResource(offenderService, contactService, convictionService, nsiService, offenderManagerService, sentenceService, userService, currentUserSupplier, custodyService, userAccessService),
                new SecureControllerAdvice()
        );
    }

    @Test
    public void getLatestRecallAndReleaseForOffender_offenderFound_returnsOk() {
        given(offenderService.offenderIdOfNomsNumber(SOME_NOMS_NUMBER)).willReturn(MAYBE_SOME_OFFENDER_ID);
        given(offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID)).willReturn(OffenderLatestRecall.builder().build());

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/nomsNumber/%s/release", SOME_NOMS_NUMBER))
                .then()
                .statusCode(200);
    }

    @Test
    public void getLatestRecallAndReleaseForOffender_offenderFound_recallDataOk() {
        given(offenderService.offenderIdOfNomsNumber(SOME_NOMS_NUMBER)).willReturn(MAYBE_SOME_OFFENDER_ID);
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(offenderRecall())
                .lastRelease(offenderRelease())
                .build();
        given(offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID)).willReturn(expectedOffenderRecall);

        final var offenderLatestRecall = given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/nomsNumber/%s/release", SOME_NOMS_NUMBER))
                .then()
                .extract()
                .body()
                .as(OffenderLatestRecall.class);

        assertThat(offenderLatestRecall).isEqualTo(expectedOffenderRecall);
    }

    @Test
    public void getLatestRecallAndReleaseForOffender_missingEstablishmentType_returnsNullEstablishmentType() {
        given(offenderService.offenderIdOfNomsNumber(SOME_NOMS_NUMBER)).willReturn(MAYBE_SOME_OFFENDER_ID);
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(offenderRecall())
                .lastRelease(offenderReleaseNotEstablishment())
                .build();
        given(offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID)).willReturn(expectedOffenderRecall);

        final var offenderLatestRecall = given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/nomsNumber/%s/release", SOME_NOMS_NUMBER))
                .then()
                .extract()
                .body()
                .as(OffenderLatestRecall.class);

        assertThat(offenderLatestRecall.getLastRelease().getInstitution().getEstablishmentType()).isNull();
    }

    @Test
    public void getLatestRecallAndReleaseForOffender_offenderNotFound_returnsNotFound() {
        given(offenderService.offenderIdOfNomsNumber(SOME_NOMS_NUMBER)).willReturn(OFFENDER_ID_NOT_FOUND);

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/nomsNumber/%s/release", SOME_NOMS_NUMBER))
                .then()
                .statusCode(404);
    }

    @Test
    public void getLatestRecallAndReleaseForOffenderByCrn_offenderFound_returnsOk() {
        given(offenderService.offenderIdOfCrn(SOME_CRN_NUMBER)).willReturn(MAYBE_SOME_OFFENDER_ID);
        given(offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID)).willReturn(OffenderLatestRecall.builder().build());

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/crn/%s/release", SOME_CRN_NUMBER))
                .then()
                .statusCode(200);

    }

    @Test
    public void getLatestRecallAndReleaseForOffenderByCrn_offenderFound_recallDataOk() {
        given(offenderService.offenderIdOfCrn(SOME_CRN_NUMBER)).willReturn(MAYBE_SOME_OFFENDER_ID);
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(offenderRecall())
                .lastRelease(offenderRelease())
                .build();
        given(offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID)).willReturn(expectedOffenderRecall);

        final var offenderLatestRecall = given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/crn/%s/release", SOME_CRN_NUMBER))
                .then()
                .extract()
                .body()
                .as(OffenderLatestRecall.class);

        assertThat(offenderLatestRecall).isEqualTo(expectedOffenderRecall);
    }

    @Test
    public void getLatestRecallAndReleaseForOffenderByCrn_offenderNotFound_returnsNotFound() {
        given(offenderService.offenderIdOfCrn(SOME_CRN_NUMBER)).willReturn(OFFENDER_ID_NOT_FOUND);

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/crn/%s/release", SOME_CRN_NUMBER))
                .then()
                .statusCode(404);

    }

    @Test
    public void getLatestRecallAndReleaseForOffender_notSingleCustodyEvent_returnsBadRequest() {
        given(offenderService.offenderIdOfCrn(SOME_CRN_NUMBER)).willReturn(MAYBE_SOME_OFFENDER_ID);
        given(offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID))
                .willThrow(ConvictionService.SingleActiveCustodyConvictionNotFoundException.class);

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/crn/%s/release", SOME_CRN_NUMBER))
                .then()
                .statusCode(400);

    }

    @Test
    public void getLatestRecallAndReleaseForOffender_notSingleCustodyEvent_returnsDetailsInErrorMessage() {
        given(offenderService.offenderIdOfCrn(SOME_CRN_NUMBER)).willReturn(MAYBE_SOME_OFFENDER_ID);
        given(offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID))
                .willThrow(new ConvictionService.SingleActiveCustodyConvictionNotFoundException(SOME_OFFENDER_ID, SOME_ACTIVE_CUSTODY_CONVICTION_COUNT));

        ErrorResponse errorResponse = given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/crn/%s/release", SOME_CRN_NUMBER))
                .then()
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(errorResponse.getDeveloperMessage()).contains(SOME_OFFENDER_ID.toString());
        assertThat(errorResponse.getDeveloperMessage()).contains(SOME_ACTIVE_CUSTODY_CONVICTION_COUNT.toString());

    }

    @Test
    public void getLatestRecallAndReleaseForOffender_custodyRecordNotFound_returnsBadRequest() {
        given(offenderService.offenderIdOfCrn(anyString())).willReturn(MAYBE_SOME_OFFENDER_ID);
        given(offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID)).willThrow(CustodyNotFoundException.class);

        given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/crn/%s/release", SOME_CRN_NUMBER))
                .then()
                .statusCode(400);
    }

    @Test
    public void getLatestRecallAndReleaseForOffender_custodyRecordNotFound_returnsEventIdInErrorMessage() {
        given(offenderService.offenderIdOfCrn(anyString())).willReturn(MAYBE_SOME_OFFENDER_ID);
        given(offenderService.getOffenderLatestRecall(SOME_OFFENDER_ID)).willThrow(new CustodyNotFoundException(SOME_CUSTODIAL_EVENT));

        ErrorResponse errorResponse = given()
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format("/secure/offenders/crn/%s/release", SOME_CRN_NUMBER))
                .then()
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(errorResponse.getDeveloperMessage()).contains(SOME_CUSTODIAL_EVENT_ID.toString());
    }

    private OffenderRecall offenderRecall() {
        return offenderRecallBuilder().build();
    }

    private OffenderRelease offenderRelease() {
        return offenderReleaseBuilder().build();
    }

    private OffenderRelease offenderReleaseNotEstablishment() {
        final var nonEstablishmentInstitution = releaseInstitutionBuilder().isEstablishment(false).establishmentType(null).build();
        return offenderReleaseBuilder().institution(nonEstablishmentInstitution).build();
    }

    private OffenderRecall.OffenderRecallBuilder offenderRecallBuilder() {
        return OffenderRecall.builder()
                .date(LocalDate.of(2019, 10, 10))
                .reason(KeyValue.builder().code("R").description("Rejected").build())
                .notes("Recall notes");
    }

    private OffenderRelease.OffenderReleaseBuilder offenderReleaseBuilder() {
        Institution releaseInstitution = releaseInstitutionBuilder().build();
        return OffenderRelease.builder()
                .date(LocalDate.of(2019, 10, 6))
                .notes("Release notes")
                .reason(KeyValue.builder().code("ADL").description("Adult Licence").build())
                .institution(releaseInstitution);
    }

    private Institution.InstitutionBuilder releaseInstitutionBuilder() {
        return Institution.builder()
                .code("BWIHMP")
                .description("Berwyn (HMP)")
                .institutionId(2500004521L)
                .institutionName("Berwyn (HMP)")
                .isEstablishment(true)
                .isPrivate(true)
                .establishmentType(KeyValue.builder().code("E").description("Prison").build())
                .nomsPrisonInstitutionCode("BWI");
    }

}

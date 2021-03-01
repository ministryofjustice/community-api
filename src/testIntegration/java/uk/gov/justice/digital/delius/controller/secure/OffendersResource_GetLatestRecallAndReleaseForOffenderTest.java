package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Institution;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRelease;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_GetLatestRecallAndReleaseForOffenderTest extends IntegrationTestBase {
    @Test
    public void getLatestRecallAndReleaseForOffender_offenderFound_returnsOk() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/release")
                .then()
                .statusCode(200);

    }

    @Test
    public void getLatestRecallAndReleaseForOffender_offenderFound_recallDataOk() {
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(expectedOffenderRecall())
                .lastRelease(expectedOffenderRelease())
                .build();

        final var offenderLatestRecall = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
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
    public void getLatestRecallAndReleaseForOffender_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/NOMS_NOT_FOUND/release")
                .then()
                .statusCode(404);
    }

    @Test
    public void getLatestRecallAndReleaseForOffenderByCrn_offenderFound_returnsOk() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/X320741/release")
                .then()
                .statusCode(200);

    }

    @Test
    public void getLatestRecallAndReleaseForOffenderByCrn_offenderFound_recallDataOk() {
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(expectedOffenderRecall())
                .lastRelease(expectedOffenderRelease())
                .build();

        final var offenderLatestRecall = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
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
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/CRN_NOT_FOUND/release")
                .then()
                .statusCode(404);

    }

    private OffenderRecall expectedOffenderRecall() {
        return expectedOffenderRecallBuilder().build();
    }

    private OffenderRelease expectedOffenderRelease() {
        return expectedOffenderReleaseBuilder().build();
    }

    private OffenderRecall.OffenderRecallBuilder expectedOffenderRecallBuilder() {
        return OffenderRecall.builder()
                .date(LocalDate.of(2019, 10, 10))
                .reason(KeyValue.builder().code("S").description("Non-Compliance").build())
                .notes("Recall notes");
    }

    private OffenderRelease.OffenderReleaseBuilder expectedOffenderReleaseBuilder() {
        Institution releaseInstitution = expectedReleaseInstitutionBuilder().build();
        return OffenderRelease.builder()
                .date(LocalDate.of(2019, 10, 6))
                .notes("Release notes")
                .reason(KeyValue.builder().code("ADL").description("Adult Licence").build())
                .institution(releaseInstitution);
    }

    private Institution.InstitutionBuilder expectedReleaseInstitutionBuilder() {
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

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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.*;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev,dev-seed")
public class OffendersResource_GetLatestRecallAndReleaseForOffenderIT {

    @LocalServerPort
    int port;

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
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(expectedOffenderRecall())
                .lastRelease(expectedOffenderRelease())
                .build();

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
    public void getLatestRecallAndReleaseForOffender_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(validOauthToken)
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
                .oauth2(validOauthToken)
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
        given()
                .auth()
                .oauth2(validOauthToken)
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
                .reason(KeyValue.builder().code("R").description("Rejected").build())
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

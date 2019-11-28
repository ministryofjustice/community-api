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
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class OffendersResource_GetLatestRecallAndReleaseForOffender {

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
                .lastRecall(getDefaultOffenderRecall())
                .lastRelease(getDefaultOffenderRelease())
                .build();
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(anyString()))
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
        final var expectedOffenderRecall = OffenderLatestRecall.builder()
                .lastRecall(getOffenderRecallNotEstablishment())
                .lastRelease(getOffenderReleaseNotEstablishment())
                .build();
        org.mockito.BDDMockito.given(mockOffenderService.getOffenderLatestRecall(anyString()))
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

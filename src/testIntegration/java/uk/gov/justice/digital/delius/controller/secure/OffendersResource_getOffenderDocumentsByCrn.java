package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusExtension;
import uk.gov.justice.digital.delius.controller.wiremock.DeliusMockServer;
import uk.gov.justice.digital.delius.data.api.ConvictionDocuments;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class OffendersResource_getOffenderDocumentsByCrn {

    private static final DeliusMockServer deliusMockServer = new DeliusMockServer(8088, "src/testIntegration/resources");
    @RegisterExtension
    static DeliusExtension deliusExtension = new DeliusExtension(deliusMockServer);

    private static final String GROUPED_DOCS_PATH_FORMAT = "/offenders/crn/%s/documents/grouped";
    private static final String SINGLE_DOC_PATH_FORMAT = "/offenders/crn/%s/documents/%s";
    public static final String EXISTING_DOCUMENT_ID = "fa63c379-8b31-4e36-a152-2a57dfe251c4";

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.good}")
    private String validOauthToken;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void singleDocument_givenCrnDoesNotMatchMetadataThenReturn404() {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(SINGLE_DOC_PATH_FORMAT, "CRNXXX", "fa63c379-8b31-4e36-a152-2a57dfe251c5"))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void singleDocument_givenUnknownDocumentIdThenReturn404() {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(SINGLE_DOC_PATH_FORMAT, "crn123", "fa63c379-8b31-4e36-a152-2a57dfe251c5"))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void singleDocument_givenExistingDocumentIdThenReturn200() {
        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(SINGLE_DOC_PATH_FORMAT, "crn123", EXISTING_DOCUMENT_ID))
                .then()
                .contentType("application/msword;charset=UTF-8")
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void givenUnknownCrnThenReturn404() {
        given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(GROUPED_DOCS_PATH_FORMAT, "CRNXXX"))
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void givenKnownCrnWithNoDocuments() {
        final OffenderDocuments offenderDocuments = given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(String.format(GROUPED_DOCS_PATH_FORMAT, "CRN12"))
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(OffenderDocuments.class);

        assertThat(offenderDocuments.getDocuments()).isNull();
        assertThat(offenderDocuments.getConvictions()).isNull();
    }

    @Test
    public void givenKnownCrnThenReturnDocuments() {
        final OffenderDocuments offenderDocuments = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
            .when()
                .get(String.format(GROUPED_DOCS_PATH_FORMAT, "X320741"))
            .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(OffenderDocuments.class);

        assertThat(offenderDocuments.getConvictions()).hasSize(2);
        assertThat(offenderDocuments.getDocuments()).hasSize(7);

        final ConvictionDocuments convictionDocuments = offenderDocuments.getConvictions().stream()
                .filter(convictionDocument -> convictionDocument.getConvictionId().equals("2500295345")).findFirst().get();

        final OffenderDocumentDetail institutionalDocument = convictionDocuments.getDocuments().stream()
            .filter(doc -> doc.getId().equals("e6cf2802-32d5-4a91-a7a9-00064d27bb19")).findFirst().get();
        assertThat(institutionalDocument.getSubType().getCode()).isEqualTo("PAR");
        assertThat(institutionalDocument.getSubType().getDescription()).isEqualTo("Parole Assessment Report");
        assertThat(institutionalDocument.getReportDocumentDates().getRequestedDate()).isEqualTo(LocalDate.of(2019, 9, 5));

        final OffenderDocumentDetail courtReportDocument = convictionDocuments.getDocuments().stream()
            .filter(doc -> doc.getId().equals("1d842fce-ec2d-45dc-ac9a-748d3076ca6b")).findFirst().get();
        assertThat(courtReportDocument.getSubType().getCode()).isEqualTo("CJF");
        assertThat(courtReportDocument.getSubType().getDescription()).isEqualTo("Pre-Sentence Report - Fast");
        assertThat(courtReportDocument.getReportDocumentDates().getRequestedDate()).isEqualTo(LocalDate.of(2018, 9, 4));
        assertThat(courtReportDocument.getReportDocumentDates().getRequiredDate()).isEqualTo(LocalDate.of(2019, 9, 4));
    }
}

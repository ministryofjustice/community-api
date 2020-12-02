package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.controller.wiremock.AlfrescoExtension;
import uk.gov.justice.digital.delius.data.api.ConvictionDocuments;
import uk.gov.justice.digital.delius.data.api.OffenderDocumentDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDocuments;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class DocumentResourceAPITest extends IntegrationTestBase {

    @DisplayName("/offenders/crn/{crn}/documents/{documentId}")
    @Nested
    @ExtendWith(AlfrescoExtension.class)
    class SingleDocument {
        public static final String EXISTING_DOCUMENT_ID = "fa63c379-8b31-4e36-a152-2a57dfe251c4";

        @Test
        @DisplayName("Will return 404 for a offender that is not found")
        public void singleDocument_givenCrnDoesNotMatchMetadataThenReturn404() {
            given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/documents/{documentId}", "CRNXXX", "fa63c379-8b31-4e36-a152-2a57dfe251c5")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Will return 404 for a document that is not found")
        public void singleDocument_givenUnknownDocumentIdThenReturn404() {
            given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/documents/{documentId}", "crn123", "fa63c379-8b31-4e36-a152-2a57dfe251c5")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Will return 200 and the document byte stream for a document for given offender")
        public void singleDocument_givenExistingDocumentIdThenReturn200() {

            given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/documents/{documentId}", "crn123", EXISTING_DOCUMENT_ID)
                .then()
                .contentType("application/msword;charset=UTF-8")
                .statusCode(HttpStatus.OK.value());
        }

    }


    @DisplayName("/offenders/crn/{crn}/documents/grouped")
    @Nested
    class DocumentsGrouped {

        @Test
        @DisplayName("Will return 404 for a offender that is not found")
        public void givenUnknownCrnThenReturn404() {
            given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/documents/grouped", "CRNXXX")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Will return empty list for a offender with no documents")
        public void givenKnownCrnWithNoDocuments() {
            final OffenderDocuments offenderDocuments = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/documents/grouped", "CRN12")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(OffenderDocuments.class);

            assertThat(offenderDocuments.getDocuments()).isNull();
            assertThat(offenderDocuments.getConvictions()).isNull();
        }

        @Test
        @DisplayName("Will return documents for an offender grouped by convictions")
        public void givenKnownCrnThenReturnDocuments() {
            final OffenderDocuments offenderDocuments = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(OffenderDocuments.class);

            assertThat(offenderDocuments.getConvictions()).hasSize(2);
            assertThat(offenderDocuments.getDocuments()).hasSize(7);

            final ConvictionDocuments convictionDocuments = offenderDocuments
                .getConvictions()
                .stream()
                .filter(convictionDocument -> convictionDocument.getConvictionId().equals("2500295345"))
                .findFirst()
                .orElseThrow();

            final OffenderDocumentDetail institutionalDocument = convictionDocuments.getDocuments().stream()
                .filter(doc -> doc.getId().equals("e6cf2802-32d5-4a91-a7a9-00064d27bb19")).findFirst().orElseThrow();
            assertThat(institutionalDocument.getSubType().getCode()).isEqualTo("PAR");
            assertThat(institutionalDocument.getSubType().getDescription()).isEqualTo("Parole Assessment Report");
            assertThat(institutionalDocument
                .getReportDocumentDates()
                .getRequestedDate()).isEqualTo(LocalDate.of(2019, 9, 5));

            final OffenderDocumentDetail courtReportDocument = convictionDocuments.getDocuments().stream()
                .filter(doc -> doc.getId().equals("1d842fce-ec2d-45dc-ac9a-748d3076ca6b")).findFirst().orElseThrow();
            assertThat(courtReportDocument.getSubType().getCode()).isEqualTo("CJF");
            assertThat(courtReportDocument.getSubType().getDescription()).isEqualTo("Pre-Sentence Report - Fast");
            assertThat(courtReportDocument
                .getReportDocumentDates()
                .getRequestedDate()).isEqualTo(LocalDate.of(2018, 9, 4));
            assertThat(courtReportDocument
                .getReportDocumentDates()
                .getRequiredDate()).isEqualTo(LocalDate.of(2019, 9, 4));
        }
    }
}

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
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class DocumentResourceAPITest extends IntegrationTestBase {

    private long countAllDocuments(OffenderDocuments offenderDocuments) {
        return allDocuments(offenderDocuments).size();
    }

    private List<OffenderDocumentDetail> allDocuments(OffenderDocuments offenderDocuments) {
        final var convictionDocuments =
            offenderDocuments
                .getConvictions()
                .stream()
                .flatMap(cd -> cd.getDocuments().stream())
                .collect(toList());

        return Stream
            .of(offenderDocuments.getDocuments(), convictionDocuments)
            .flatMap(Collection::stream)
            .collect(toList());
    }

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
    class DocumentsGroupedByCRN {
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

            assertThat(offenderDocuments.getDocuments()).isEmpty();
            assertThat(offenderDocuments.getConvictions()).isEmpty();
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

        @DisplayName("Validation of request")
        @Nested
        class ValidationOfRequest {
            @Test
            @DisplayName("ok when no category or type supplied")
            void oKWhenNoCategoryOrTypeSupplied() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            }

            @Test
            @DisplayName("ok with a known category is supplied")
            void okWithAKnownCategoryIsSupplied() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            }

            @Test
            @DisplayName("ok with a known category with a related known type")
            void okWithAKnownCategoryWithARelatedKnownType() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .param("subtype", "PSR")
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            }

            @Test
            @DisplayName("bad request when category is unknown")
            void badRequestWhenCategoryIsUnknown() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "BANANAS")
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());

            }

            @Test
            @DisplayName("bad request when type is unknown")
            void badRequestWhenTypeIsUnknown() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .param("subtype", "BANANAS")
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
            }

            @Test
            @DisplayName("bad request when only type is supplied")
            void badRequestWhenOnlyTypeIsSupplied() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("subtype", "PSR")
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
            }

            @Test
            @DisplayName("bad request when type does no belong with category")
            void badRequestWhenTypeDoesNoBelongWithCategory() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "CASE_ALLOCATION_DOCUMENT")
                    .param("subtype", "PSR")
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
            }
        }

        @DisplayName("Filters")
        @Nested
        class Filters {
            static final int NSI_DOCUMENT_COUNT = 3;
            static final int ALL_COURT_REPORT_DOCUMENT_COUNT = 2;
            static final int PSR_COURT_REPORT_DOCUMENT_COUNT = 1;

            @Test
            @DisplayName("With no filter all documents will be returned")
            void withNoFilterAllDocumentsWillBeReturned() {
                final var offenderDocuments = given()
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

                assertThat(countAllDocuments(offenderDocuments)).isGreaterThan(ALL_COURT_REPORT_DOCUMENT_COUNT + PSR_COURT_REPORT_DOCUMENT_COUNT);
            }

            @Test
            @DisplayName("With category filter only documents in that category will be returned")
            void withCategoryFilterOnlyDocumentsInThatCategoryWillBeReturned() {
                final var courtReportDocuments = given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .body()
                    .as(OffenderDocuments.class);

                assertThat(countAllDocuments(courtReportDocuments)).isEqualTo(ALL_COURT_REPORT_DOCUMENT_COUNT);

                final var nsiDocuments = given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "NSI_DOCUMENT")
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .body()
                    .as(OffenderDocuments.class);

                assertThat(countAllDocuments(nsiDocuments)).isEqualTo(NSI_DOCUMENT_COUNT);
            }

            @Test
            @DisplayName("With category and type supplied only documents in that category that match the type will be returned")
            void withCategoryAndTypeSuppliedOnlyDocumentsInThatCategoryThatMatchTheTypeWillBeReturned() {
                final var courtReportDocuments = given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .param("subtype", "PSR")
                    .get("/offenders/crn/{crn}/documents/grouped", "X320741")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .body()
                    .as(OffenderDocuments.class);

                assertThat(countAllDocuments(courtReportDocuments)).isEqualTo(PSR_COURT_REPORT_DOCUMENT_COUNT);
            }
        }
    }
    @DisplayName("/offenders/nomsNumber/{nomsNumber}/documents/grouped")
    @Nested
    class DocumentsGroupedByNOMSNumber {
        @Test
        @DisplayName("Will return 404 for a offender that is not found")
        public void givenUnknownNomsNumberThenReturn404() {
            given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9999XX")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Will return empty list for a offender with no documents")
        public void givenKnownNomsNumberWithNoDocuments() {
            final OffenderDocuments offenderDocuments = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G4106UN")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(OffenderDocuments.class);

            assertThat(offenderDocuments.getDocuments()).isEmpty();
            assertThat(offenderDocuments.getConvictions()).isEmpty();
        }

        @Test
        @DisplayName("Will return documents for an offender grouped by convictions")
        public void givenKnownNomsNumberThenReturnDocuments() {
            final OffenderDocuments offenderDocuments = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
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

        @DisplayName("Validation of request")
        @Nested
        class ValidationOfRequest {
            @Test
            @DisplayName("ok when no category or type supplied")
            void oKWhenNoCategoryOrTypeSupplied() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            }

            @Test
            @DisplayName("ok with a known category is supplied")
            void okWithAKnownCategoryIsSupplied() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            }

            @Test
            @DisplayName("ok with a known category with a related known type")
            void okWithAKnownCategoryWithARelatedKnownType() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .param("subtype", "PSR")
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.OK.value());
            }

            @Test
            @DisplayName("bad request when category is unknown")
            void badRequestWhenCategoryIsUnknown() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "BANANAS")
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());

            }

            @Test
            @DisplayName("bad request when type is unknown")
            void badRequestWhenTypeIsUnknown() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .param("subtype", "BANANAS")
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
            }

            @Test
            @DisplayName("bad request when only type is supplied")
            void badRequestWhenOnlyTypeIsSupplied() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("subtype", "PSR")
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
            }

            @Test
            @DisplayName("bad request when type does no belong with category")
            void badRequestWhenTypeDoesNoBelongWithCategory() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "CASE_ALLOCATION_DOCUMENT")
                    .param("subtype", "PSR")
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
            }
        }

        @DisplayName("Filters")
        @Nested
        class Filters {
            static final int NSI_DOCUMENT_COUNT = 3;
            static final int ALL_COURT_REPORT_DOCUMENT_COUNT = 2;
            static final int PSR_COURT_REPORT_DOCUMENT_COUNT = 1;

            @Test
            @DisplayName("With no filter all documents will be returned")
            void withNoFilterAllDocumentsWillBeReturned() {
                final var offenderDocuments = given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .body()
                    .as(OffenderDocuments.class);

                assertThat(countAllDocuments(offenderDocuments)).isGreaterThan(ALL_COURT_REPORT_DOCUMENT_COUNT + PSR_COURT_REPORT_DOCUMENT_COUNT);
            }

            @Test
            @DisplayName("With category filter only documents in that category will be returned")
            void withCategoryFilterOnlyDocumentsInThatCategoryWillBeReturned() {
                final var courtReportDocuments = given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .body()
                    .as(OffenderDocuments.class);

                assertThat(countAllDocuments(courtReportDocuments)).isEqualTo(ALL_COURT_REPORT_DOCUMENT_COUNT);

                final var nsiDocuments = given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "NSI_DOCUMENT")
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .body()
                    .as(OffenderDocuments.class);

                assertThat(countAllDocuments(nsiDocuments)).isEqualTo(NSI_DOCUMENT_COUNT);
            }

            @Test
            @DisplayName("With category and type supplied only documents in that category that match the type will be returned")
            void withCategoryAndTypeSuppliedOnlyDocumentsInThatCategoryThatMatchTheTypeWillBeReturned() {
                final var courtReportDocuments = given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .param("type", "COURT_REPORT_DOCUMENT")
                    .param("subtype", "PSR")
                    .get("/offenders/nomsNumber/{nomsNumber}/documents/grouped", "G9542VP")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .body()
                    .as(OffenderDocuments.class);

                assertThat(countAllDocuments(courtReportDocuments)).isEqualTo(PSR_COURT_REPORT_DOCUMENT_COUNT);
            }
        }
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.data.api.SentenceStatus;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.justice.digital.delius.transformers.SentenceStatusTransformer.NO_CUSTODY_CODE;
import static uk.gov.justice.digital.delius.transformers.SentenceStatusTransformer.NO_CUSTODY_DESCRIPTION;

public class OffendersResource_getOffenderSentenceStatusByCrnTest extends IntegrationTestBase{

    private static final Long KNOWN_CONVICTION_ID = 2600295124L;
    private static final Long CONVICTION_ID_NO_CUSTODY = 2600295125L;
    private static final Long KNOWN_SENTENCE_ID = 2600282123L;
    private static final Long SENTENCE_ID_NO_CUSTODY = 2600282124L;
    private static final String KNOWN_CRN = "X320811";

    @Deprecated(forRemoval = true)
    @Nested
    @DisplayName("Tests for get sentence status with sentence ID")
    class WithSentenceId {

        public static final String URL_TEMPLATE = "/offenders/crn/%s/convictions/%s/sentences/%s/status";

        @Test
        public void getSentenceStatus() {

            final var sentenceStatus = given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(URL_TEMPLATE, KNOWN_CRN, KNOWN_CONVICTION_ID, KNOWN_SENTENCE_ID))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(SentenceStatus.class);

            assertThat(sentenceStatus.getSentenceId()).isEqualTo(KNOWN_SENTENCE_ID);
            assertThat(sentenceStatus.getSentence().getDescription()).isEqualTo("CJA - Indeterminate Public Prot.");
            assertThat(sentenceStatus.getSentenceDate()).isEqualTo(LocalDate.of(2018, 12, 3));
            assertThat(sentenceStatus.getCustodialType().getCode()).isEqualTo("P");
            assertThat(sentenceStatus.getCustodialType().getDescription()).isEqualTo("Post Sentence Supervision");
            assertThat(sentenceStatus.getMainOffence().getDescription()).isEqualTo("Common assault and battery - 10501");
            assertThat(sentenceStatus.getActualReleaseDate()).isEqualTo(LocalDate.of(2019, 7, 3));
            assertThat(sentenceStatus.getLicenceExpiryDate()).isEqualTo(LocalDate.of(2019, 11, 3));
            assertThat(sentenceStatus.getLength()).isEqualTo(11);
            assertThat(sentenceStatus.getLengthUnit()).isEqualTo("Months");

        }

        @Test
        public void givenSentenceNoCustody_whenGetSentenceStatus_thenReturnOk() {

            final var sentenceStatus = given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(URL_TEMPLATE, KNOWN_CRN, CONVICTION_ID_NO_CUSTODY, SENTENCE_ID_NO_CUSTODY))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(SentenceStatus.class);

            assertThat(sentenceStatus.getSentenceId()).isEqualTo(SENTENCE_ID_NO_CUSTODY);
            assertThat(sentenceStatus.getCustodialType().getCode()).isEqualTo(NO_CUSTODY_CODE);
            assertThat(sentenceStatus.getCustodialType().getDescription()).isEqualTo(NO_CUSTODY_DESCRIPTION);
            assertThat(sentenceStatus.getActualReleaseDate()).isNull();
            assertThat(sentenceStatus.getLicenceExpiryDate()).isNull();
        }

        @Test
        public void getSentenceStatus_sentenceDoesNotExist() {
            String path = String.format(URL_TEMPLATE, KNOWN_CRN, KNOWN_CONVICTION_ID, 11111111L);

            given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        }

        @Test
        public void getSentenceStatus_convictionDoesNotExist() {
            String path = String.format(URL_TEMPLATE, KNOWN_CRN, 111111111L, KNOWN_SENTENCE_ID);

            given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        }

        @Test
        public void getSentenceStatus_offenderDoesNotExist() {
            String path = String.format(URL_TEMPLATE, "XBADBAD", KNOWN_CONVICTION_ID, KNOWN_SENTENCE_ID);

            given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        }
    }

    @Nested
    @DisplayName("Tests for get sentence status without sentence ID")
    class WithoutSentenceId {

        public static final String URL_TEMPLATE = "/offenders/crn/%s/convictions/%s/sentenceStatus";

        @Test
        public void getSentenceStatus() {

            final var sentenceStatus = given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(URL_TEMPLATE, KNOWN_CRN, KNOWN_CONVICTION_ID))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(SentenceStatus.class);

            assertThat(sentenceStatus.getSentenceId()).isEqualTo(KNOWN_SENTENCE_ID);
            assertThat(sentenceStatus.getSentence().getDescription()).isEqualTo("CJA - Indeterminate Public Prot.");
            assertThat(sentenceStatus.getSentenceDate()).isEqualTo(LocalDate.of(2018, 12, 3));
            assertThat(sentenceStatus.getCustodialType().getCode()).isEqualTo("P");
            assertThat(sentenceStatus.getCustodialType().getDescription()).isEqualTo("Post Sentence Supervision");
            assertThat(sentenceStatus.getMainOffence().getDescription()).isEqualTo("Common assault and battery - 10501");
            assertThat(sentenceStatus.getActualReleaseDate()).isEqualTo(LocalDate.of(2019, 7, 3));
            assertThat(sentenceStatus.getLicenceExpiryDate()).isEqualTo(LocalDate.of(2019, 11, 3));
            assertThat(sentenceStatus.getLength()).isEqualTo(11);
            assertThat(sentenceStatus.getLengthUnit()).isEqualTo("Months");

        }

        @Test
        public void givenSentenceNoCustody_whenGetSentenceStatus_thenReturnOk() {

            final var sentenceStatus = given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(String.format(URL_TEMPLATE, KNOWN_CRN, CONVICTION_ID_NO_CUSTODY))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(SentenceStatus.class);

            assertThat(sentenceStatus.getSentenceId()).isEqualTo(SENTENCE_ID_NO_CUSTODY);
            assertThat(sentenceStatus.getCustodialType().getCode()).isEqualTo(NO_CUSTODY_CODE);
            assertThat(sentenceStatus.getCustodialType().getDescription()).isEqualTo(NO_CUSTODY_DESCRIPTION);
            assertThat(sentenceStatus.getActualReleaseDate()).isNull();
            assertThat(sentenceStatus.getLicenceExpiryDate()).isNull();
        }

        @Test
        public void getSentenceStatus_noSentenceOnConviction() {
            String path = String.format(URL_TEMPLATE, KNOWN_CRN, 2600295126L);

            given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        public void getSentenceStatus_convictionDoesNotExist() {
            String path = String.format(URL_TEMPLATE, KNOWN_CRN, 111111111L);

            given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        }

        @Test
        public void getSentenceStatus_offenderDoesNotExist() {
            String path = String.format(URL_TEMPLATE, "XBADBAD", KNOWN_CONVICTION_ID);

            given()
                .auth()
                .oauth2(createJwt("ROLE_COMMUNITY"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        }
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.data.api.CustodialStatus;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class OffendersResource_getOffenderCustodialStatusByCrnTest {
    private static final Long KNOWN_CONVICTION_ID = 2600295124L;
    private static final Long KNOWN_SENTENCE_ID = 2600282123L;
    private static final String KNOWN_CRN = "X320811";
    public static final String URL_TEMPLATE = "/offenders/crn/%s/convictions/%s/sentences/%s/custodialStatus";

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.token.good}")
    private String validOauthToken;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void getCustodialStatus() {
        String path = String.format(URL_TEMPLATE, KNOWN_CRN, KNOWN_CONVICTION_ID, KNOWN_SENTENCE_ID);

        final var custodialStatus = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(CustodialStatus.class);

        assertThat(custodialStatus.getSentenceId()).isEqualTo(KNOWN_SENTENCE_ID);
        assertThat(custodialStatus.getSentence().getDescription()).isEqualTo("CJA - Indeterminate Public Prot.");
        assertThat(custodialStatus.getSentenceDate()).isEqualTo(LocalDate.of(2018, 12, 3));
        assertThat(custodialStatus.getCustodialType().getCode()).isEqualTo("P");
        assertThat(custodialStatus.getCustodialType().getDescription()).isEqualTo("Post Sentence Supervision");
        assertThat(custodialStatus.getMainOffence().getDescription()).isEqualTo("Common assault and battery - 10501");
        assertThat(custodialStatus.getActualReleaseDate()).isEqualTo(LocalDate.of(2019, 7, 3));
        assertThat(custodialStatus.getLicenceExpiryDate()).isEqualTo(LocalDate.of(2019, 11, 3));
        assertThat(custodialStatus.getLength()).isEqualTo(11);
        assertThat(custodialStatus.getLengthUnit()).isEqualTo("Months");

    }

    @Test
    public void getCustodialStatus_sentenceDoesNotExist() {
        String path = String.format(URL_TEMPLATE, KNOWN_CRN, KNOWN_CONVICTION_ID, 11111111L);

        final var custodialStatus = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

    }

    @Test
    public void getCustodialStatus_convictionDoesNotExist() {
        String path = String.format(URL_TEMPLATE, KNOWN_CRN, 111111111L, KNOWN_SENTENCE_ID);

        final var custodialStatus = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

    }

    @Test
    public void getCustodialStatus_offenderDoesNotExist() {
        String path = String.format(URL_TEMPLATE, "XBADBAD", KNOWN_CONVICTION_ID, KNOWN_SENTENCE_ID);

        final var custodialStatus = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

    }
}

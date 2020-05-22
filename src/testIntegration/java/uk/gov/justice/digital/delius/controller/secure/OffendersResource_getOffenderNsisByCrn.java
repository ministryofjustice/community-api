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
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-seed")
public class OffendersResource_getOffenderNsisByCrn {

    private static final String OFFENDERS_PATH = "/offenders/crn/%s/convictions/%s/nsis?";

    private static final String QUERY_PARAM_NAME = "nsiCodes";

    private static final Long KNOWN_CONVICTION_ID = 2500295345L;

    private static final Long KNOWN_CONVICTION_ID_NO_BREACH = 2500295343L;

    private static final String KNOWN_OFFENDER = "X320741";
    private static final String GET_NSI_PATH = "/offenders/crn/%s/convictions/%s/nsis/%s";
    private static final Long KNOWN_NSI_ID = 2500018597L;
    public static final int UNKNOWN_NSI_ID = 12234566;
    private static final long KNOWN_CONVICTION_ID_FOR_NSI = 2500295345L;
    private static final String KNOWN_CRN_FOR_NSI = "X320741";

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
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void getNsiByCrnAndNsiId() {
        String path = String.format(GET_NSI_PATH, KNOWN_CRN_FOR_NSI, KNOWN_CONVICTION_ID_FOR_NSI, KNOWN_NSI_ID);

        final var nsi = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(Nsi.class);

        assertThat(nsi.getNsiId()).isEqualTo(KNOWN_NSI_ID);
        assertThat(nsi.getLength()).isEqualTo(20L);
        assertThat(nsi.getNsiManagers().get(0).getProbationArea().getDescription()).isEqualTo("NPS North East");
        assertThat(nsi.getNsiManagers().get(0).getProbationArea().getCode()).isEqualTo("N02");
        assertThat(nsi.getNsiManagers().get(0).getTeam().getDescription()).isEqualTo("Unallocated Team(N02)");
        assertThat(nsi.getNsiManagers().get(0).getTeam().getCode()).isEqualTo("N02UAT");
        assertThat(nsi.getNsiManagers().get(0).getStaff().getStaff().getForenames()).isEqualTo("Unallocated Staff(N02)");
        assertThat(nsi.getNsiManagers().get(0).getStaff().getStaff().getSurname()).isEqualTo("Staff");
    }

    @Test
    public void givenNonExistentOffender_whenGetNsiByCrnNsiId_then404() {
        String path = String.format(GET_NSI_PATH, "UNKNOWN_OFFENDER", KNOWN_CONVICTION_ID_FOR_NSI, KNOWN_NSI_ID);

        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void givenNonExistentConviction_whenGetNsiByCrnNsiId_then404() {
        String path = String.format(GET_NSI_PATH, KNOWN_CRN_FOR_NSI, 123435789L, KNOWN_NSI_ID);

        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void givenNonExistentNsi_whenGetNsiByCrnNsiId_then404() {
        String path = String.format(GET_NSI_PATH, KNOWN_CRN_FOR_NSI, KNOWN_CONVICTION_ID_FOR_NSI, UNKNOWN_NSI_ID);

        given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void getGetOffenderNsisByCrnAndConvictionId() {
        final String path = String.format(OFFENDERS_PATH, KNOWN_OFFENDER, KNOWN_CONVICTION_ID)
                + QUERY_PARAM_NAME + "=BRE&" + QUERY_PARAM_NAME + "=BRES";
        final var nsiWrapper = given()
                .auth()
                .oauth2(validOauthToken)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(NsiWrapper.class);

        assertThat(nsiWrapper.getNsis()).hasSize(2);
    }

    @Test
    public void getGetOffenderNsisByCrnAndKnownConvictionIdButNoMatchToFilter() {
        final String path = String.format(OFFENDERS_PATH, KNOWN_OFFENDER, KNOWN_CONVICTION_ID) + QUERY_PARAM_NAME + "=XXX";
        final var nsiWrapper = given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(NsiWrapper.class);

        assertThat(nsiWrapper.getNsis()).isEmpty();
    }

    @Test
    public void getGetOffenderNsisByCrnAndKnownConvictionIdForOffenderButNotNsi() {
        final String path = String.format(OFFENDERS_PATH, KNOWN_OFFENDER, "2500297061")
            + QUERY_PARAM_NAME + "=BRE&" + QUERY_PARAM_NAME + "=BRES";
        final var nsiWrapper = given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(NsiWrapper.class);

        assertThat(nsiWrapper.getNsis()).isEmpty();
    }

    @Test
    public void getGetOffenderNsisByCrnAndConvictionIdWithNoNsi() {
        final String path = String.format(OFFENDERS_PATH, KNOWN_OFFENDER, KNOWN_CONVICTION_ID_NO_BREACH)
            + QUERY_PARAM_NAME + "=BRE&" + QUERY_PARAM_NAME + "=BRES";
        final var nsiWrapper = given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(NsiWrapper.class);

        assertThat(nsiWrapper.getNsis()).isEmpty();
    }

    @Test
    public void getGetOffenderNsisByCrnAndConvictionIdLimitCodeFetchOneOfTwo() {

        final String path = String.format(OFFENDERS_PATH, KNOWN_OFFENDER, KNOWN_CONVICTION_ID) + QUERY_PARAM_NAME + "=BRES";
        final var nsiWrapper = given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .body()
            .as(NsiWrapper.class);

        assertThat(nsiWrapper.getNsis()).hasSize(1);
    }

    @Test
    public void getGetOffenderNsisByCrnAndUnknownConvictionId_Returns404() {

        final String path = String.format(OFFENDERS_PATH, KNOWN_OFFENDER, "0")
            + QUERY_PARAM_NAME + "=BRE&" + QUERY_PARAM_NAME + "=BRES";
        given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void getOffenderNsisByCrn_offenderNotFound_returnsNotFound() {

        final String path = String.format(OFFENDERS_PATH, "X777777", KNOWN_CONVICTION_ID)
            + QUERY_PARAM_NAME + "=BRE&" + QUERY_PARAM_NAME + "=BRES";
        given()
            .auth()
            .oauth2(validOauthToken)
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.data.api.NsiWrapper;

import java.time.LocalDate;
import java.time.Month;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getNsiForOffenderByCrnAndActiveConvictions extends IntegrationTestBase {
    private static final String QUERY_PARAM_NSI_CODES = "nsiCodes";
    private static final String GET_NSI_PATH = "/offenders/crn/%s/convictions/active/nsis?";
    private static final Long KNOWN_NSI_ID = 2500018597L;
    private static final String KNOWN_CRN_FOR_NSI = "X320741";

    @Test
    public void nsiForActiveConvictionExists() {
        //There is a soft deleted 'QWERT' NSI that should be ignored
        final String path = String.format(GET_NSI_PATH, KNOWN_CRN_FOR_NSI)
                + QUERY_PARAM_NSI_CODES + "=BRE&" + QUERY_PARAM_NSI_CODES + "=QWERT";

        final var nsi = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(NsiWrapper.class);
        assertThat(nsi.getNsis()).hasSize(1);
        assertThat(nsi.getNsis().get(0).getNsiId()).isEqualTo(KNOWN_NSI_ID);
        assertThat(nsi.getNsis().get(0).getReferralDate()).isEqualTo(LocalDate.of(2019, Month.SEPTEMBER, 2));
    }

    @Test
    public void unknownOffender() {
        final String path = String.format(GET_NSI_PATH, "UNKNOWN_CRN")
                + QUERY_PARAM_NSI_CODES + "=BRE";

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get(path)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }


    @Test
    public void noNSIsOfProvidedTypeExist() {
        final String path = String.format(GET_NSI_PATH, KNOWN_CRN_FOR_NSI)
                + QUERY_PARAM_NSI_CODES + "=XXX";
        final var nsiWrapper = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
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
}

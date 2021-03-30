package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Offence;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getOffenderConvictionsByNoms extends IntegrationTestBase {
    @Test
    public void canGetOffenderConvictionsByNoms() {
        final var convictions = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP/convictions")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Conviction[].class);

        final var conviction = Stream.of(convictions).filter(Conviction::getActive).findAny().orElseThrow();
        final var offence = conviction.getOffences().stream().filter(Offence::getMainOffence).findAny().orElseThrow();
        assertThat(offence.getDetail().getCode()).isEqualTo("00102");
        KeyValue sentenceType = conviction.getSentence().getSentenceType();
        assertThat(sentenceType.getCode()).isEqualTo("SC");
        assertThat(sentenceType.getDescription()).isEqualTo("CJA - Indeterminate Public Prot.");
    }


    @Test
    public void getOffenderConvictionsByNoms_offenderNotFound_returnsNotFound() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/NOTFOUND/convictions")
                .then()
                .statusCode(404);
    }

    @Test
    public void canGetActiveConvictionsOnly(){
        final var convictions = given()
            .auth()
            .oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/nomsNumber/G9542VP/convictions?activeOnly=true")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .as(Conviction[].class);

        final var inactiveConviction = Stream.of(convictions).filter(c -> c.getActive() == false).findAny();
        assertThat(inactiveConviction.isPresent()).isFalse();
    }
}

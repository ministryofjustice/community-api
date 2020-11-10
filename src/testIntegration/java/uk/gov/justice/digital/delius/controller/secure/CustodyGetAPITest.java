package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class CustodyGetAPITest extends IntegrationTestBase {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String CRN = "X320741";
    private static final long CONVICTION_ID = 2500295345L;
    private static final String PRISON_BOOKING_NUMBER = "V74111";

    @Nested
    @DisplayName("GET offenders/nomsNumber/{nomsNumber}/custody/bookingNumber/{bookingNumber}")
    class getCustodyByBookNumber {
        @Test
        @DisplayName("must have role ROLE_COMMUNITY")
        public void mustHaveRoleCommunity() {
            final var token = createJwt("ROLE_BANANAS");

            given()
                    .auth().oauth2(token)
                    .contentType("application/json")
                    .when()
                    .get("offenders/nomsNumber/{nomsNumber}/custody/bookingNumber/{bookingNumber}", NOMS_NUMBER, PRISON_BOOKING_NUMBER)
                    .then()
                    .statusCode(403);
        }

        @Test
        @DisplayName("can get custody information by noms number and book number")
        public void canGetByNomsNumberAndBookNumber() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                    .auth().oauth2(token)
                    .when()
                    .get("offenders/nomsNumber/{nomsNumber}/custody/bookingNumber/{bookingNumber}", NOMS_NUMBER, PRISON_BOOKING_NUMBER)
                    .then()
                    .statusCode(200)
                    .body("bookingNumber", equalTo("V74111"))
                    .body("institution.code", equalTo("BWIHMP"))
                    .body("institution.description", equalTo("Berwyn (HMP)"))
                    .body("status.code", equalTo("D"))
                    .body("status.description", equalTo("In Custody"))
                    .body("keyDates.size()", equalTo(0))
            ;
        }

    }

    @Nested
    @DisplayName("GET offenders/crn/{crn}/custody/convictionId/{convictionId}")
    class getCustodyByConvictionId {
        @Test
        @DisplayName("must have role ROLE_COMMUNITY")
        public void mustHaveRoleCommunity() {
            final var token = createJwt("ROLE_BANANAS");

            given()
                    .auth().oauth2(token)
                    .when()
                    .get("offenders/crn/{crn}/custody/convictionId/{convictionId}", CRN, CONVICTION_ID)
                    .then()
                    .statusCode(403);
        }

        @Test
        @DisplayName("can get custody information by crn and conviction id")
        public void canGetByNomsNumberAndBookNumber() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                    .auth().oauth2(token)
                    .when()
                    .get("offenders/crn/{crn}/custody/convictionId/{convictionId}", CRN, CONVICTION_ID)
                    .then()
                    .statusCode(200)
                    .body("bookingNumber", equalTo("V74111"))
                    .body("institution.code", equalTo("BWIHMP"))
                    .body("institution.description", equalTo("Berwyn (HMP)"))
                    .body("status.code", equalTo("D"))
                    .body("status.description", equalTo("In Custody"))
                    .body("keyDates.size()", equalTo(0))
            ;
        }

    }
}

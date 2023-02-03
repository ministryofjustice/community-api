package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith({SpringExtension.class, FlywayRestoreExtension.class})
@DisplayName("Smoke test update helpers")
public class SmokeTestHelperAPITest extends IntegrationTestBase {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String CRN = "X320741";
    private static final long CONVICTION_ID = 2500295345L;
    private static final String PRISON_BOOKING_NUMBER = "V74111";

    @Nested
    @DisplayName("POST smoketest/offenders/crn/{crn}/custody/reset")
    class CustodyReset {
        @Test
        @DisplayName("must have `ROLE_SMOKE_TEST` to access this service")
        public void mustHaveCommunityRole() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .post("smoketest/offenders/crn/{crn}/custody/reset", CRN)
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("Can reset custody data")
        public void canResetCustodyData() {
            final var token = createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE", "ROLE_COMMUNITY", "ROLE_SMOKE_TEST");

            // Given current location is HMP Moorland
            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateCustody("MDI"))
                .when()
                .put(format("offenders/nomsNumber/%s/custody/bookingNumber/%s", NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

            // AND the conviction has some keys dates
            given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createReplaceCustodyKeyDates(ReplaceCustodyKeyDates
                    .builder()
                    .conditionalReleaseDate(LocalDate.now())
                    .licenceExpiryDate(LocalDate.now())
                    .hdcEligibilityDate(LocalDate.now())
                    .paroleEligibilityDate(LocalDate.now())
                    .sentenceExpiryDate(LocalDate.now())
                    .expectedReleaseDate(LocalDate.now())
                    .postSentenceSupervisionEndDate(LocalDate.now())
                    .build()))
                .when()
                .post(String.format("offenders/nomsNumber/%s/bookingNumber/%s/custody/keyDates", NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);


            // WHEN when I reset this data
            given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .post("/smoketest/offenders/crn/{crn}/custody/reset", CRN)
                .then()
                .statusCode(200);


            // AND the offender no longer has a NOMS number
            given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/{nomsNumber}/all", NOMS_NUMBER)
                .then()
                .statusCode(404);

            // AND I can reset again with no errors
            given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .post("/smoketest/offenders/crn/{crn}/custody/reset", CRN)
                .then()
                .statusCode(200);
        }

        private String createUpdateCustody(@SuppressWarnings("SameParameterValue") String prisonCode) {
            return writeValueAsString(UpdateCustody
                .builder()
                .nomsPrisonInstitutionCode(prisonCode)
                .build());
        }

        private String createReplaceCustodyKeyDates(ReplaceCustodyKeyDates replaceCustodyKeyDates) {
            return writeValueAsString(replaceCustodyKeyDates);
        }
    }
    @Nested
    @DisplayName("POST smoketest/offenders/crn/{crn}/details")
    class UpdateOffenderDetails {
        @Test
        @DisplayName("must have `ROLE_SMOKE_TEST` to access this service")
        public void mustHaveCommunityRole() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .body(createOffenderDetails("Betty", "Boop"))
                .when()
                .post("/smoketest/offenders/crn/{crn}/details", CRN)
                .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("will reject updates with no names")
        public void mustHaveNamesInBody() {
            final var token = createJwt("ROLE_SMOKE_TEST");

            given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .body(createOffenderDetails("", ""))
                .when()
                .post("/smoketest/offenders/crn/{crn}/details", CRN)
                .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Can set first name and surname")
        public void canUpdateOffenderDetails() {
            final var token = createJwt("ROLE_SMOKE_TEST");

            // GIVEN I change the name
            given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .body(createOffenderDetails("Betty", "Boop"))
                .when()
                .post("smoketest/offenders/crn/{crn}/details", CRN)
                .then()
                .statusCode(200);


            // WHEN I the retrieve the details
            final var offenderDetail = given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/all", CRN)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderDetail.class);

            // THEN the name would reflect the name previously saved
            assertThat(offenderDetail.getFirstName()).isEqualTo("Betty");
            assertThat(offenderDetail.getSurname()).isEqualTo("Boop");

        }

        private String createOffenderDetails(@SuppressWarnings("SameParameterValue") String firstName, @SuppressWarnings("SameParameterValue") String surname) {
            return writeValueAsString(uk.gov.justice.digital.delius.data.api.UpdateOffenderDetails
                .builder()
                .firstName(firstName)
                .surname(surname)
                .build());
        }
    }
}

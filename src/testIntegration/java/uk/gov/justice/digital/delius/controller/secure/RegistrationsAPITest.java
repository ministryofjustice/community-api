package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RegistrationsAPITest extends IntegrationTestBase {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String CRN = "X320741";

    @Test
    public void canGetRegistrationByNOMSNumber() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/{nomsNumber}/registrations", NOMS_NUMBER)
                .then()
                .statusCode(200)
                .body("registrations[2].register.description", is("Public Protection"))
                .body("registrations[2].deregisteringNotes", nullValue())
                .body("registrations[3].deregisteringNotes", is("Ok again now"))
                .body("registrations[3].numberOfPreviousDeregistrations", is(2));
    }

    @Test
    public void canGetRegistrationByCRN() {
        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/crn/{crn}/registrations", CRN)
                .then()
                .statusCode(200)
                .body("registrations[2].register.description", is("Public Protection"))
                .body("registrations[2].deregisteringNotes", nullValue())
                .body("registrations[3].deregisteringNotes", is("Ok again now"));
    }

    @Test
    public void canGetActiveRegistrationByCRN() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/{crn}/registrations?activeOnly=true", CRN)
            .then()
            .statusCode(200).body("registrations", hasSize(2));
    }

    @Test
    public void cantGetActiveRegistrationByCRN_ifExcluded() {
        given()
            .auth().oauth2(createJwtWithUsername("bob.jones", "ROLE_COMMUNITY"))
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/X440877/registrations")
            .then()
            .statusCode(403)
            .body("developerMessage", equalTo("You are excluded from viewing this offender record. Please contact a system administrator"));
    }

    @Test
    public void canGetIndividualRegistrationByCRNAndRegistrationId() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .contentType(APPLICATION_JSON_VALUE)
            .when()
            .get("/offenders/crn/{crn}/registrations/{registrationId}", CRN, 2500094760L)
            .then()
            .statusCode(200)
            .body("register.description", is("Public Protection"))
            .body("registrationId", is(2500094760L))
            .body("registrationReviews[0].reviewDate", is("2020-04-08"))
            .body("registrationReviews[0].reviewDateDue", is("2021-04-08"))
            .body("registrationReviews[0].notes", is("Bad"))
            .body("registrationReviews[0].reviewingTeam.code", is("N02T01"))
            .body("registrationReviews[0].reviewingTeam.description", is("OMU A "))
            .body("registrationReviews[0].reviewingOfficer.code", is("N02P008"))
            .body("registrationReviews[0].reviewingOfficer.forenames", is("Alfie ZZ"))
            .body("registrationReviews[0].reviewingOfficer.surname", is("Rhenal"))
            .body("registrationReviews[0].completed", is(false));
    }

    @Nested
    @DisplayName("When multiple records match the same noms number")
    class DuplicateNOMSNumbers{
        @Nested
        @DisplayName("When only one of the records is current")
        class OnlyOneActive{
            @Test
            @DisplayName("will return the active record")
            void willReturnTheActiveRecord() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3232DD/registrations")
                    .then()
                    .statusCode(200);
            }
            @Test
            @DisplayName("will return a conflict response when fail on duplicate is set to true")
            void willReturnAConflictResponseWhenFailureOnDuplicate() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3232DD/registrations?failOnDuplicate=true")
                    .then()
                    .statusCode(409);
            }

        }
        @Nested
        @DisplayName("When both records have the same active state")
        class BothActive{
            @Test
            @DisplayName("will return a conflict response")
            void willReturnAConflictResponse() {
                given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nomsNumber/G3636DD/registrations")
                    .then()
                    .statusCode(409);
            }
        }
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;

public class OffendersResource_getActivityLogByCrnTest extends IntegrationTestBase {
    @Test
    public void happyPath() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/activity-log?convictionDatesOf=2500295345")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("totalPages", greaterThan(0))
            .body("totalElements", greaterThan(0))
            .body("size", equalTo(1000))
            .body("numberOfElements", greaterThan(0))
            .body("content.size()", greaterThan(0))

            // Conviction level RAR appointment
            .root("content.find { it.date == '2017-12-02' }.entries[0]")
            .body("contactId", equalTo(2503537768L))
            .body("convictionId", equalTo(2500295345L))
            .body("startTime", equalTo("12:00:00"))
            .body("endTime", equalTo("13:00:00"))
            .body("type.code", equalTo("CHVS"))
            .body("type.description", equalTo("Home Visit to Case (NS)"))
            .body("type.appointment", equalTo(true))
            .body("type.nationalStandard", equalTo(true))
            .body("type.systemGenerated", equalTo(false))
            .body("notes", equalTo("Some RAR notes"))
            .body("rarActivity.requirementId", equalTo(2500083653L))
            .body("rarActivity.nsiId", equalTo(2500018999L))
            .body("rarActivity.type.code", equalTo("APCUS"))
            .body("rarActivity.type.description", equalTo("Custody - Accredited Programme"))
            .body("rarActivity.subtype.code", equalTo("APHSP"))
            .body("rarActivity.subtype.description", equalTo("Healthy Sex Programme (HCP)"))
            .body("lastUpdatedDateTime", equalTo("2019-09-04T00:00:00+01:00"))
            .body("lastUpdatedByUser.forenames", equalTo("Andy"))
            .body("lastUpdatedByUser.surname", equalTo("Marke"))
            .body("enforcement.enforcementAction.code", equalTo("WLS"))
            .body("enforcement.enforcementAction.description", equalTo("Enforcement Letter Requested"))

            // Offender level, non-RAR
            .root("content.find { it.date == '2021-02-01' }.entries.find { it.contactId == 2503537765 }")
            .body("convictionId", nullValue())
            .body("type.code", equalTo("ERGD"))
            .body("rar", nullValue());
    }

    @Test
    public void rarFilter() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/activity-log?convictionDatesOf=2500295345&rarActivity=true")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", equalTo(1))
            .body("size", equalTo(1000))
            .body("numberOfElements", equalTo(1))
            .body("content.size()", equalTo(1))
            .body("content[0].rarDay", equalTo(true));
    }

    @Test
    public void missingRequiredRole() {
        given()
            .auth().oauth2(createJwt("ROLE_DUMMY"))
            .when()
            .get("/offenders/crn/any_crn/activity-log")
            .then()
            .assertThat()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void invalidRequest() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/any_crn/activity-log?page=0&pageSize=0")
            .then()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void offenderDoesNotExist() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/some_missing_crn/activity-log")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}

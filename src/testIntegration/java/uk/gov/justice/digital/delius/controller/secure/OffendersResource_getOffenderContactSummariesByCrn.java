package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.specification.Argument;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class OffendersResource_getOffenderContactSummariesByCrn extends IntegrationTestBase {
    private final List<Argument> registration = withArgs(2503537767L);
    private final List<Argument> appointment = withArgs(2502719240L);

    @Test
    public void gettingOffenderContactSummariesByCrn() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?page=0&pageSize=10")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(false))
            .body("totalPages", greaterThan(1))
            .body("totalElements", greaterThan(10))
            .body("size", equalTo(10))
            .body("numberOfElements", equalTo(10))
            .body("content.size()", equalTo(10))
            .root("content.find { it.contactId == %d }")

            .body("contactStart", registration, equalTo("2021-02-01T00:00:00Z"))
            .body("contactEnd", registration, equalTo("2021-02-01T00:00:00Z"))
            .body("type.code", registration, equalTo("ERGN"))
            .body("type.description", registration, equalTo("New Registration"))
            .body("type.shortDescription", registration, equalTo("ERGN - SGC"))
            .body("type.appointment", registration, equalTo(false))
            .body("type.systemGenerated", registration, equalTo(true))
            .body("type.categories.size()", registration, equalTo(2))
            .body("type.categories[0].code", registration, equalTo("RR"))
            .body("type.categories[0].description", registration, equalTo("Risk & Registers"))
            .body("type.categories[1].code", registration, equalTo("AL"))
            .body("type.categories[1].description", registration, equalTo("All/Always"))
            .body("officeLocation", registration, equalTo(null))
            .body("notes", registration, equalTo("""
                Type: Public Protection - MAPPA
                Next Review Date: 01/05/2021 00:00:00
                Level: MAPPA Level 2
                Category: MAPPA Cat 2
                Notes: X320741 registering MAPPA cat 2 level 2"""))
            .body("provider.code", registration, equalTo("BMI"))
            .body("provider.description", registration, equalTo("Birmingham (HMP)"))
            .body("team.code", registration, equalTo("N02AAM"))
            .body("team.description", registration, equalTo("OMIC OMU A "))
            .body("staff.code", registration, equalTo("N02AAMU"))
            .body("staff.forenames", registration, equalTo("Unallocated"))
            .body("staff.surname", registration, equalTo("Staff"))
            .body("staff.unallocated", registration, equalTo(true))
            .body("sensitive", registration, equalTo(null))
            .body("outcome", registration, equalTo(null))

            .body("contactStart", appointment, equalTo("2020-09-04T00:00:00+01:00"))
            .body("contactEnd", appointment, equalTo("2020-09-04T00:00:00+01:00"))
            .body("type.appointment", appointment, equalTo(true))
            .body("outcome.code", appointment, equalTo("APPK"))
            .body("outcome.description", appointment, equalTo("Appointment Kept"))
            .body("outcome.attended", appointment, equalTo(true))
            .body("outcome.complied", appointment, equalTo(null))
            .body("outcome.hoursCredited", appointment, equalTo(null))
            .body("rarActivity", appointment, equalTo(false));
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByAppointmentOnly() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?appointmentsOnly=true")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", equalTo(3))
            .body("size", equalTo(1000))
            .body("numberOfElements", equalTo(3))
            .body("content.size()", equalTo(3))
            .root("content.find { it.contactId == %d }")

            .body("", registration, equalTo(null))

            .body("", appointment, notNullValue());
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByConvictionId() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?convictionId=2500297061")
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
            .root("content.find { it.contactId == %d }")

            .body("", registration, equalTo(null))

            .body("", withArgs(2502726145L), notNullValue());
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByAttendedTrue() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?attended=true")
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
            .root("content.find { it.contactId == %d }")

            .body("", registration, equalTo(null))

            .body("", withArgs(2502719240L), notNullValue())
            .body("outcome.attended", withArgs(2502719240L), equalTo(true));
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByAttendedFalse() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?attended=false")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", equalTo(2))
            .body("size", equalTo(1000))
            .body("numberOfElements", equalTo(2))
            .body("content.size()", equalTo(2))
            .root("content.find { it.contactId == %d }")

            .body("", registration, equalTo(null))

            .body("", withArgs(2502719244L), notNullValue())
            .body("outcome.attended", withArgs(2502719244L), equalTo(false));
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByCompliedTrue() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?complied=true")
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
            .root("content.find { it.contactId == %d }")

            .body("", registration, equalTo(null))

            .body("", withArgs(2502719245L), notNullValue());
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByCompliedFalse() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?complied=false")
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
            .root("content.find { it.contactId == %d }")

            .body("", withArgs(2502719244L), notNullValue());
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByNationalStandardTrue() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?nationalStandard=true")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", equalTo(4))
            .body("size", equalTo(1000))
            .body("numberOfElements", equalTo(4))
            .body("content.size()", equalTo(4))
            .root("content.find { it.contactId == %d }")

            .body("", registration, equalTo(null))

            .body("", withArgs(2502719244L), notNullValue())
            .body("type.description", withArgs(2502719244L), equalTo("3 Way Meeting (NS)"))

            .body("", withArgs(2502719245L), notNullValue())
            .body("type.description", withArgs(2502719245L), equalTo("3 Way Meeting (NS)"))

            .body("", withArgs(2502719240L), notNullValue())
            .body("type.description", withArgs(2502719240L), equalTo("3 Way Meeting (NS)"))

            .body("", withArgs(2502719239L), notNullValue())
            .body("type.description", withArgs(2502719239L), equalTo("Alcohol Screening"))

            .body("", withArgs(2502743375L), nullValue())
            .body("type.nationalStandard", withArgs(2502719239L), equalTo(true));

    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByNationalStandardFalse() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?nationalStandard=false")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", greaterThan(10))
            .body("size", equalTo(1000))
            .body("numberOfElements", greaterThan(10))
            .body("content.size()", greaterThan(10))
            .root("content.find { it.contactId == %d }")

            .body("", withArgs(2502743375L), notNullValue())
            .body("type.description", withArgs(2502743375L), equalTo("NOMIS Case Notes - General"))
            .body("type.nationalStandard", withArgs(2502743375L), equalTo(false));
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByContactDate() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?contactDateFrom=2020-09-04&contactDateTo=2020-09-04&contactDateFrom=2020-09-04")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", greaterThan(1))
            .body("size", equalTo(1000))
            .body("numberOfElements", greaterThan(1))
            .body("content.size()", greaterThan(1))
            .root("content.find { it.contactId == %d }")

            .body("", withArgs(2502719245L), notNullValue())
            .body("contactStart", withArgs(2502719245L), equalTo("2020-09-04T00:00:00+01:00"));
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByOutcomeTrue() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?outcome=true")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", equalTo(3))
            .body("size", equalTo(1000))
            .body("numberOfElements", equalTo(3))
            .body("content.size()", equalTo(3))
            .root("content.find { it.contactId == %d }")

            .body("", withArgs(2502719244L), notNullValue());
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByOutcomeFalse() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?outcome=false")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", greaterThan(40))
            .body("size", equalTo(1000))
            .body("numberOfElements", greaterThan(40))
            .body("content.size()", greaterThan(40))
            .root("content.find { it.contactId == %d }")

            .body("", withArgs(2512709905L), notNullValue());
    }

    @Test
    public void gettingOffenderContactSummariesByCrnAndFilteringByIncludeTypesAndAppointments() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary?include=appointments&include=type_eter&include=type_ccmp")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("last", equalTo(true))
            .body("totalPages", equalTo(1))
            .body("totalElements", equalTo(7))
            .body("size", equalTo(1000))
            .body("numberOfElements", equalTo(7))
            .body("content.size()", equalTo(7))
            .root("content.find { it.contactId == %d }")

            // appointments
            .body("type.code", withArgs(2502719240L), equalTo("C084"))
            .body("type.appointment", withArgs(2502719240L), equalTo(true))

            .body("type.code", withArgs(2502719245L), equalTo("C084"))
            .body("type.appointment", withArgs(2502719245L), equalTo(true))

            .body("type.code", withArgs(2502719245L), equalTo("C084"))
            .body("type.appointment", withArgs(2502719245L), equalTo(true))

            .body("type.code", withArgs(2502719244L), equalTo("C084"))
            .body("type.appointment", withArgs(2502719244L), equalTo(true))

            // type matching CCMP
            .body("type.code", withArgs(2502721488L), equalTo("CCMP"))
            .body("type.appointment", withArgs(2502721488L), equalTo(false))

            // type matching ETER
            .body("type.code", withArgs(2512709905L), equalTo("ETER"))
            .body("type.appointment", withArgs(2512709905L), equalTo(false))

            .body("type.code", withArgs(2502709905L), equalTo("ETER"))
            .body("type.appointment", withArgs(2502709905L), equalTo(false))

            .body("type.code", withArgs(2502709898L), equalTo("ETER"))
            .body("type.appointment", withArgs(2502709898L), equalTo(false));
    }

    @Test
    public void gettingOffenderContactSummariesByCrnDefaultsToFirstPage() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/X320741/contact-summary")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("number", equalTo(0))
            .body("first", equalTo(true))
            .body("size", equalTo(1000))
            .body("content.size()", greaterThan(0));
    }

    @Test
    public void gettingOffenderContactSummariesByCrnButInvalidRequest() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/any_crn/contact-summary?page=0&pageSize=0")
            .then()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void gettingOffenderContactSummariesByCrnButOffenderDoesNotExist() {
        given()
            .auth().oauth2(tokenWithRoleCommunity())
            .when()
            .get("/offenders/crn/some_missing_crn/contact-summary")
            .then()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}

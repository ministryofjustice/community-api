package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffendersResource_getPrimaryIdentifiersAPITest extends IntegrationTestBase {

    public static final int TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS = 23;
    public static final int NUMBER_OF_DELETED_RECORDS = 2;

    @Test
    @DisplayName("Must have COMMUNITY_API role")
    void mustHaveCommunityRole() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/primaryIdentifiers")
                .then()
                .statusCode(200);


        given()
                .auth()
                .oauth2(createJwt("ROLE_BANANAS"))
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/primaryIdentifiers")
                .then()
                .statusCode(403);

    }

    @Test
    @DisplayName("Will return all offenders when page size is huge")
    public void willReturnAllOffenders() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .param("size", 1000)
                .when()
                .get("/offenders/primaryIdentifiers")
                .then()
                .statusCode(200)
                .body("content.size()", is(TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS))
                .body("totalElements", is(TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS))
                .body("numberOfElements", is(TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS))
                .body("size", is(1000))
                .body("content.find { it.offenderId == 2500343964 }.crn", is("X320741"))
                .body("content[0].offenderId", notNullValue())
                .body("content[0].crn", notNullValue());
    }

    @Test
    @DisplayName("Will return first 5 offenders sorted with small page size")
    public void willReturnPagedOffenders() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .param("size", 5)
                .param("sort", "crn,desc")
                .when()
                .get("/offenders/primaryIdentifiers")
                .then()
                .statusCode(200)
                .body("content.size()", is(5))
                .body("totalElements", is(TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS))
                .body("numberOfElements", is(5))
                .body("size", is(5))
                .body("content.find { it.offenderId == 2500343964 }.crn", is("X320741"));
    }

    @Test
    @DisplayName("Can page through the offenders")
    public void canPageThroughOffenders() {
        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .param("size", 5)
                .param("sort", "crn,desc")
                .param("page", "0")
                .when()
                .get("/offenders/primaryIdentifiers")
                .then()
                .statusCode(200)
                .body("content.size()", is(5))
                .body("totalElements", is(TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS))
                .body("numberOfElements", is(5))
                .body("size", is(5))
                .body("number", is(0))
                .body("content.find { it.offenderId == 2500343964 }.crn", is("X320741"));

        given()
                .auth()
                .oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .param("size", 5)
                .param("sort", "crn,desc")
                .param("page", "1")
                .when()
                .get("/offenders/primaryIdentifiers")
                .then()
                .statusCode(200)
                .body("content.size()", is(5))
                .body("totalElements", is(TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS))
                .body("numberOfElements", is(5))
                .body("size", is(5))
                .body("number", is(1))
                .body("content.find { it.offenderId == 2500343964 }.crn", nullValue());
    }

    @Nested
    class Filters {
        @Test
        @DisplayName("Deleted records are filtered out by default")
        void byDefaultDeletedRecordsAreFilteredOut() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("content.size()", is(TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS))
                    .body("content.find { it.offenderId == 31 }.crn", nullValue());
        }

        @Test
        @DisplayName("Deleted records can be included")
        void canIncludeDeletedRecords() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("includeDeleted", true)
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("content.size()", is(TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS + NUMBER_OF_DELETED_RECORDS))
                    .body("content.find { it.offenderId == 31 }.crn", is("CRN31"));
        }

        @Test
        @DisplayName("Can filter by active offenders with current sentences")
        void canFilterByActiveOffenders() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("includeActiveOnly", true)
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("content.size()", is(5))
                    .body("content.find { it.offenderId == 2500343964 }.crn", is("X320741"));
        }

        @Nested
        class ActiveDateFilter {
            @BeforeEach
            void setUp() {
            /*
                Offender has following sentences - as shown by seed data
                Sentence : 01-JUN-17 to 01-DEC-17
                Sentence : 01-JAN-18 to 31-JAN-18 (10001)- deleted
                Sentence : 01-FEB-18 to 01-MAR-18 (10002)
                Sentence : 02-FEB-18 to 22-FEB-18 (10003)
                Sentence : 16-SEP-19 to 17-SEP-19
                Sentence : 20-SEP-19 to still active
             */
            }
        }

        @Test
        @DisplayName("Not included if no events on that data")
        void notIncludedIfNoSentencesOnThatDate() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("activeDate", "2017-05-31")
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("totalElements", is(0));
        }
        @Test
        @DisplayName("Included if active sentence on that date")
        void includeIfActiveSentenceOnThatDate() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("activeDate", "2017-06-02")
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("totalElements", is(1))
                    .body("content.find { it.offenderId == 2500343964 }.crn", is("X320741"));
        }
        @Test
        @DisplayName("Included if active sentence on first day of sentence")
        void includeIfActiveSentenceStartedOnThatDate() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("activeDate", "2017-06-01")
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("totalElements", is(1))
                    .body("content.find { it.offenderId == 2500343964 }.crn", is("X320741"));
        }
        @Test
        @DisplayName("Included if active sentence on last day of sentence")
        void includeIfActiveSentenceEndedOnThatDate() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("activeDate", "2017-12-01")
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("totalElements", is(1))
                    .body("content.find { it.offenderId == 2500343964 }.crn", is("X320741"));
        }
        @Test
        @DisplayName("Not included if event deleted on that data")
        void notIncludedIfEVentDeletedOnThatDate() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("activeDate", "2018-01-02")
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("totalElements", is(0));
        }

        @Test
        @DisplayName("Include only once if two active sentences on that date")
        void includeOnceIfTwoActiveSentencesOnThatDate() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("activeDate", "2018-02-03")
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("totalElements", is(1))
                    .body("content.find { it.offenderId == 2500343964 }.crn", is("X320741"));
        }

        @Test
        @DisplayName("Included for today if sentence has not ended")
        void canFilterByActiveOffendersOnCertainDate() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("activeDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("totalElements", is(2))
                    .body("content.find { it.offenderId == 2500343964 }.crn", is("X320741"));

        }
    }

    @Nested
    class Sorting {
        @Test
        @DisplayName("Can sort by CRN ascending")
        void canSortByCRNAscending() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("sort", "crn,desc")
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("content[0].crn", is("X320811"));

        }
        @Test
        @DisplayName("Default sort is offenderId ascending")
        void defaultSortIsOffenderIdAscending() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body("content[0].offenderId", is(11))
                    .body("content[1].offenderId", is(12));
        }

        @Test
        @DisplayName("Can sort by CRN descending")
        void canSortByCRNDescending() {
            given()
                    .auth()
                    .oauth2(tokenWithRoleCommunity())
                    .contentType(APPLICATION_JSON_VALUE)
                    .param("size", 1000)
                    .param("sort", "crn,asc")
                    .when()
                    .get("/offenders/primaryIdentifiers")
                    .then()
                    .statusCode(200)
                    .body(String.format("content[%d].crn", TOTAL_NUMBER_NON_DELETED_OF_OFFENDERS - 1), is("X320811"));
        }
    }
}

package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.JwtAuthenticationHelper;
import uk.gov.justice.digital.delius.data.api.CreateCustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("dev-seed")
@DirtiesContext
public class CustodyKeyDatesAPITest {
    private static final String OFFENDER_ID = "2500343964";
    private static final String CRN = "X320741";
    private static final String CRN_NO_EVENTS = "CRN31";
    private static final String OFFENDER_ID_NO_EVENTS = "31";
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String PRISON_BOOKING_NUMBER = "V74111";

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;
    @Autowired
    private Flyway flyway;
    private static Flyway flywayInstance;

    @Value("${test.token.good}")
    private String validOauthToken;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
        //noinspection SqlWithoutWhere
        jdbcTemplate.execute("DELETE FROM KEY_DATE");
        //noinspection SqlWithoutWhere
        jdbcTemplate.execute("DELETE FROM CONTACT");
        flywayInstance = flyway;
    }

    @AfterAll
    public static void after() {
        flywayInstance.clean();
        flywayInstance.migrate();
    }


    @Test
    public void anAddedKeyDateCanBeRetrievedByCRN()  {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate.class);

        assertThat(addedKeyDate.getDate()).isEqualTo(tomorrow);
        assertThat(addedKeyDate.getType().getCode()).isEqualTo("POM1");
        assertThat(addedKeyDate.getType().getDescription()).isEqualTo("POM Handover expected start date");
    }

    private String createCustodyKeyDateOf(LocalDate tomorrow) {
        try {
            return objectMapper.writeValueAsString(CreateCustodyKeyDate
                    .builder()
                    .date(tomorrow)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void anAddedKeyDateCanBeRetrievedByNOMSNumber() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate.class);

        assertThat(addedKeyDate.getDate()).isEqualTo(tomorrow);
        assertThat(addedKeyDate.getType().getCode()).isEqualTo("POM1");
        assertThat(addedKeyDate.getType().getDescription()).isEqualTo("POM Handover expected start date");
    }
    @Test
    public void anAddedKeyDateCanBeRetrievedByOffenderId() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate.class);

        assertThat(addedKeyDate.getDate()).isEqualTo(tomorrow);
        assertThat(addedKeyDate.getType().getCode()).isEqualTo("POM1");
        assertThat(addedKeyDate.getType().getDescription()).isEqualTo("POM Handover expected start date");
    }
    @Test
    public void anAddedKeyDateCanBeRetrievedByBookingNumber()  {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate.class);

        assertThat(addedKeyDate.getDate()).isEqualTo(tomorrow);
        assertThat(addedKeyDate.getType().getCode()).isEqualTo("POM1");
        assertThat(addedKeyDate.getType().getDescription()).isEqualTo("POM Handover expected start date");
    }

    @Test
    public void allKeyDatesCanBeRetrievedForAnOffender()  {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM2",  CRN))
                .then()
                .statusCode(200);

        CustodyKeyDate[] keyDatesByCRN = given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/crn/%s/custody/keyDates",  CRN))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate[].class);

        CustodyKeyDate[] keyDatesByNOMSNumber = given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/nomsNumber/%s/custody/keyDates",  NOMS_NUMBER))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate[].class);

        CustodyKeyDate[] keyDatesByOffenderId = given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/offenderId/%s/custody/keyDates",  OFFENDER_ID))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate[].class);

        CustodyKeyDate[] keyDatesByBookingNumber = given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/prisonBookingNumber/%s/custody/keyDates", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate[].class);

        assertThat(keyDatesByCRN).contains(
                CustodyKeyDate
                        .builder()
                        .date(tomorrow)
                        .type(KeyValue
                                .builder()
                                .code("POM1")
                                .description("POM Handover expected start date")
                                .build())
                        .build(),
                CustodyKeyDate
                        .builder()
                        .date(tomorrow)
                        .type(KeyValue
                                .builder()
                                .code("POM2")
                                .description("RO responsibility handover from POM to OM expected date")
                                .build())
                        .build()
                )
                .isEqualTo(keyDatesByNOMSNumber)
                .isEqualTo(keyDatesByOffenderId)
                .isEqualTo(keyDatesByBookingNumber);
    }

    @Test
    public void anExistingKeyDateCanBeUpdated()  {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate.class);

        assertThat(addedKeyDate.getDate()).isEqualTo(dayAfterNext);
        assertThat(addedKeyDate.getType().getCode()).isEqualTo("POM1");
        assertThat(addedKeyDate.getType().getDescription()).isEqualTo("POM Handover expected start date");
    }

    @Test
    public void anExistingKeyDateCanBeDeletedByCRN()  {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .delete(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(404);
    }
    @Test
    public void anExistingKeyDateCanBeDeletedByNOMSNumber()  {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .delete(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(404);
    }

    @Test
    public void anExistingKeyDateCanBeDeletedByOffenderId()  {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .delete(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(404);
    }
    @Test
    public void anExistingKeyDateCanBeDeletedByBookingNumber()  {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .delete(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldRespond404WhenOffenderNotFound()  {
        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put("offenders/nomsNumber/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .delete("offenders/nomsNumber/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get("offenders/nomsNumber/DOESNOTEXIST/custody/keyDates")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put("offenders/crn/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .delete("offenders/crn/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get("offenders/crn/DOESNOTEXIST/custody/keyDates")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put("offenders/offenderId/999999999/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get("offenders/offenderId/999999999/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get("offenders/offenderId/999999999/custody/keyDates")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put("offenders/prisonBookingNumber/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .delete("offenders/prisonBookingNumber/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .auth().oauth2(validOauthToken)
                .when()
                .get("offenders/prisonBookingNumber/DOESNOTEXIST/custody/keyDates")
                .then()
                .statusCode(404);


    }

    @Test
    public void shouldRespond400WhenAddingKeyDateThatIsNotValid()  {
        JsonPath  errorMessage = given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/DOESNOTEXIST",  CRN))
                .then()
                .statusCode(400)
                .extract()
                .body()
                .jsonPath();

        assertThat(errorMessage.getString("message")).isEqualTo("DOESNOTEXIST is not a valid custody key date");
    }

    @Test
    public void shouldRespond400WhenOffenderHasInvalidActiveCustodyEvents()  {
        JsonPath errorMessage = given()
                .auth().oauth2(validOauthToken)
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN_NO_EVENTS))
                .then()
                .statusCode(400)
                .extract()
                .body()
                .jsonPath();

        assertThat(errorMessage.getString("developerMessage")).isEqualTo(String.format("Expected offender %s to have a single custody related event but found 0 events", OFFENDER_ID_NO_EVENTS));
    }

    @Nested
    class PostAllKeyDates {
        @Test
        void withoutUpdateRoleAccessWillBeDenied() {
            final var token = createJwt("ROLE_COMMUNITY");

            given()
                    .auth().oauth2(token)
                    .contentType("application/json")
                    .body(createReplaceCustodyKeyDates())
                    .when()
                    .post(String.format("offenders/nomsNumber/%s/bookingNumber/%s/custody/keyDates",  NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                    .then()
                    .statusCode(403);

        }
        @Test
        void withUpdateRoleAccessWillBeAllowed() {
            final var token = createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE");

            given()
                    .auth().oauth2(token)
                    .contentType("application/json")
                    .body(createReplaceCustodyKeyDates())
                    .when()
                    .post(String.format("offenders/nomsNumber/%s/bookingNumber/%s/custody/keyDates",  NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                    .then()
                    .statusCode(200);

        }

        @Test
        void custodyKeyDatesCanBeInsertedAndUpdatedAndDeletedWhilePOMKeyDatesRemainUnchanged() {

            // WHEN I add new sentence dates
            var conditionalReleaseDate = LocalDate.of(2030, 1, 1);
            var licenceExpiryDate = LocalDate.of(2030, 1, 2);
            var hdcEligibilityDate = LocalDate.of(2030, 1, 3);
            var paroleEligibilityDate = LocalDate.of(2030, 1, 4);
            var sentenceExpiryDate = LocalDate.of(2030, 1, 5);
            var expectedReleaseDate = LocalDate.of(2030, 1, 6);
            var postSentenceSupervisionEndDate = LocalDate.of(2030, 1, 7);


            var custodyJson = given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE"))
                    .contentType("application/json")
                    .body(createReplaceCustodyKeyDates(ReplaceCustodyKeyDates
                            .builder()
                            .conditionalReleaseDate(conditionalReleaseDate)
                            .licenceExpiryDate(licenceExpiryDate)
                            .hdcEligibilityDate(hdcEligibilityDate)
                            .paroleEligibilityDate(paroleEligibilityDate)
                            .sentenceExpiryDate(sentenceExpiryDate)
                            .expectedReleaseDate(expectedReleaseDate)
                            .postSentenceSupervisionEndDate(postSentenceSupervisionEndDate)
                            .build()))
                    .when()
                    .post(String.format("offenders/nomsNumber/%s/bookingNumber/%s/custody/keyDates",  NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath();

            // THEN the new dates will be returned from service
            assertThat(custodyJson.getString("keyDates.conditionalReleaseDate")).isEqualTo("2030-01-01");
            assertThat(custodyJson.getString("keyDates.licenceExpiryDate")).isEqualTo("2030-01-02");
            assertThat(custodyJson.getString("keyDates.hdcEligibilityDate")).isEqualTo("2030-01-03");
            assertThat(custodyJson.getString("keyDates.paroleEligibilityDate")).isEqualTo("2030-01-04");
            assertThat(custodyJson.getString("keyDates.sentenceExpiryDate")).isEqualTo("2030-01-05");
            assertThat(custodyJson.getString("keyDates.expectedReleaseDate")).isEqualTo("2030-01-06");
            assertThat(custodyJson.getString("keyDates.postSentenceSupervisionEndDate")).isEqualTo("2030-01-07");

            assertThat(custodyJson.getString("keyDates.expectedPrisonOffenderManagerHandoverStartDate")).isNull();
            assertThat(custodyJson.getString("keyDates.expectedPrisonOffenderManagerHandoverDate")).isNull();


            // AND contact should be created with new dates
            var contact = jdbcTemplate.query(
                    "SELECT * from CONTACT where OFFENDER_ID = ?",
                    List.of(OFFENDER_ID).toArray(),
                    new ColumnMapRowMapper())
                    .stream()
                    .filter(record -> toLocalDate(record.get("CONTACT_DATE")).equals(LocalDate.now()))
                    .filter(record -> record.get("EVENT_ID") != null)
                    .findFirst()
                    .orElseThrow();

            assertThat(contact.get("NOTES").toString())
                    .contains("Conditional Release Date: 01/01/2030")
                    .contains("Licence Expiry Date: 02/01/2030")
                    .contains("HDC Eligibility Date: 03/01/2030")
                    .contains("Parole Eligibility Date: 04/01/2030")
                    .contains("Sentence Expiry Date: 05/01/2030")
                    .contains("Expected Release Date: 06/01/2030")
                    .contains("PSS End Date: 07/01/2030");


            // GIVEN I hae added some POM key dates from OMiC
            given()
                    .auth().oauth2(validOauthToken)
                    .contentType("application/json")
                    .body(createCustodyKeyDateOf(LocalDate.of(2030, 1, 8)))
                    .when()
                    .put(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                    .then()
                    .statusCode(200);

            given()
                    .auth().oauth2(validOauthToken)
                    .contentType("application/json")
                    .body(createCustodyKeyDateOf(LocalDate.of(2030, 1, 9)))
                    .when()
                    .put(String.format("offenders/nomsNumber/%s/custody/keyDates/POM2",  NOMS_NUMBER))
                    .then()
                    .statusCode(200);


            conditionalReleaseDate = LocalDate.of(2031, 1, 1);
            licenceExpiryDate = LocalDate.of(2031, 1, 2);
            hdcEligibilityDate = LocalDate.of(2031, 1, 3);
            paroleEligibilityDate = LocalDate.of(2031, 1, 4);
            sentenceExpiryDate = LocalDate.of(2031, 1, 5);
            expectedReleaseDate = LocalDate.of(2031, 1, 6);
            postSentenceSupervisionEndDate = LocalDate.of(2031, 1, 7);

            // WHEN I Update the other custody key dates
            custodyJson = given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE"))
                    .contentType("application/json")
                    .body(createReplaceCustodyKeyDates(ReplaceCustodyKeyDates
                            .builder()
                            .conditionalReleaseDate(conditionalReleaseDate)
                            .licenceExpiryDate(licenceExpiryDate)
                            .hdcEligibilityDate(hdcEligibilityDate)
                            .paroleEligibilityDate(paroleEligibilityDate)
                            .sentenceExpiryDate(sentenceExpiryDate)
                            .expectedReleaseDate(expectedReleaseDate)
                            .postSentenceSupervisionEndDate(postSentenceSupervisionEndDate)
                            .build()))
                    .when()
                    .post(String.format("offenders/nomsNumber/%s/bookingNumber/%s/custody/keyDates",  NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath();

            // THEN the updated dates should be returned from the service
            assertThat(custodyJson.getString("keyDates.conditionalReleaseDate")).isEqualTo("2031-01-01");
            assertThat(custodyJson.getString("keyDates.licenceExpiryDate")).isEqualTo("2031-01-02");
            assertThat(custodyJson.getString("keyDates.hdcEligibilityDate")).isEqualTo("2031-01-03");
            assertThat(custodyJson.getString("keyDates.paroleEligibilityDate")).isEqualTo("2031-01-04");
            assertThat(custodyJson.getString("keyDates.sentenceExpiryDate")).isEqualTo("2031-01-05");
            assertThat(custodyJson.getString("keyDates.expectedReleaseDate")).isEqualTo("2031-01-06");
            assertThat(custodyJson.getString("keyDates.postSentenceSupervisionEndDate")).isEqualTo("2031-01-07");

            // AND also the POM dates that were updated separately
            assertThat(custodyJson.getString("keyDates.expectedPrisonOffenderManagerHandoverStartDate")).isEqualTo("2030-01-08");
            assertThat(custodyJson.getString("keyDates.expectedPrisonOffenderManagerHandoverDate")).isEqualTo("2030-01-09");

            //noinspection SqlWithoutWhere
            jdbcTemplate.execute("DELETE FROM CONTACT");


            // WHEN I remove the key custody dates
            custodyJson = given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE"))
                    .contentType("application/json")
                    .body(createReplaceCustodyKeyDates(ReplaceCustodyKeyDates
                            .builder()
                            .build()))
                    .when()
                    .post(String.format("offenders/nomsNumber/%s/bookingNumber/%s/custody/keyDates",  NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath();

            // THEN the return custody dates show be nul
            assertThat(custodyJson.getString("keyDates.conditionalReleaseDate")).isNull();
            assertThat(custodyJson.getString("keyDates.licenceExpiryDate")).isNull();
            assertThat(custodyJson.getString("keyDates.hdcEligibilityDate")).isNull();
            assertThat(custodyJson.getString("keyDates.paroleEligibilityDate")).isNull();
            assertThat(custodyJson.getString("keyDates.sentenceExpiryDate")).isNull();
            assertThat(custodyJson.getString("keyDates.expectedReleaseDate")).isNull();
            assertThat(custodyJson.getString("keyDates.postSentenceSupervisionEndDate")).isNull();

            // AND the existing POM key dates should remain
            assertThat(custodyJson.getString("keyDates.expectedPrisonOffenderManagerHandoverStartDate")).isEqualTo("2030-01-08");
            assertThat(custodyJson.getString("keyDates.expectedPrisonOffenderManagerHandoverDate")).isEqualTo("2030-01-09");

            // AND contact should be created with removed items
            contact = jdbcTemplate.query(
                    "SELECT * from CONTACT where OFFENDER_ID = ?",
                    List.of(OFFENDER_ID).toArray(),
                    new ColumnMapRowMapper())
                    .stream()
                    .filter(record -> toLocalDate(record.get("CONTACT_DATE")).equals(LocalDate.now()))
                    .filter(record -> record.get("EVENT_ID") != null)
                    .findFirst()
                    .orElseThrow();

            assertThat(contact.get("NOTES").toString())
                    .contains("Removed Conditional Release Date: 01/01/2031")
                    .contains("Removed Licence Expiry Date: 02/01/2031")
                    .contains("Removed HDC Eligibility Date: 03/01/2031")
                    .contains("Removed Parole Eligibility Date: 04/01/2031")
                    .contains("Removed Sentence Expiry Date: 05/01/2031")
                    .contains("Removed Expected Release Date: 06/01/2031")
                    .contains("Removed PSS End Date: 07/01/2031");

        }

        private String createReplaceCustodyKeyDates() {
            return createReplaceCustodyKeyDates(ReplaceCustodyKeyDates
                    .builder()
                    .sentenceExpiryDate(LocalDate.now())
                    .build());
        }
        private String createReplaceCustodyKeyDates(ReplaceCustodyKeyDates replaceCustodyKeyDates) {
            try {
                return objectMapper.writeValueAsString(replaceCustodyKeyDates);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String createJwt(final String ...roles ) {
        return jwtAuthenticationHelper.createJwt(JwtAuthenticationHelper.JwtParameters.builder()
                .username("APIUser")
                .roles(List.of(roles))
                .scope(Arrays.asList("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }

    private LocalDate toLocalDate(Object columnValue) {
        return ((Timestamp)columnValue).toLocalDateTime().toLocalDate();
    }

}

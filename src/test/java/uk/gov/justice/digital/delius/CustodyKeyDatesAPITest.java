package uk.gov.justice.digital.delius;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.CreateCustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.util.TokenHelper;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
@DirtiesContext
public class CustodyKeyDatesAPITest implements TokenHelper {
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
    private Jwt jwt;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
        jdbcTemplate.execute("DELETE FROM KEY_DATE");
    }

    @Test
    public void anAddedKeyDateCanBeRetrievedByCRN() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .header("Authorization", aValidToken())
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

    private String createCustodyKeyDateOf(LocalDate tomorrow) throws JsonProcessingException {
        return objectMapper.writeValueAsString(CreateCustodyKeyDate
                .builder()
                .date(tomorrow)
                .build());
    }

    @Test
    public void anAddedKeyDateCanBeRetrievedByNOMSNumber() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .header("Authorization", aValidToken())
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
    public void anAddedKeyDateCanBeRetrievedByOffenderId() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .header("Authorization", aValidToken())
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
    public void anAddedKeyDateCanBeRetrievedByBookingNumber() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .header("Authorization", aValidToken())
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
    public void allKeyDatesCanBeRetrievedForAnOffender() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM2",  CRN))
                .then()
                .statusCode(200);

        CustodyKeyDate[] keyDatesByCRN = given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/crn/%s/custody/keyDates",  CRN))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate[].class);

        CustodyKeyDate[] keyDatesByNOMSNumber = given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/nomsNumber/%s/custody/keyDates",  NOMS_NUMBER))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate[].class);

        CustodyKeyDate[] keyDatesByOffenderId = given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/offenderId/%s/custody/keyDates",  OFFENDER_ID))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CustodyKeyDate[].class);

        CustodyKeyDate[] keyDatesByBookingNumber = given()
                .header("Authorization", aValidToken())
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
    public void anExistingKeyDateCanBeUpdated() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        CustodyKeyDate addedKeyDate = given()
                .header("Authorization", aValidToken())
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
    public void anExistingKeyDateCanBeDeletedByCRN() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .delete(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN))
                .then()
                .statusCode(404);
    }
    @Test
    public void anExistingKeyDateCanBeDeletedByNOMSNumber() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .delete(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/nomsNumber/%s/custody/keyDates/POM1",  NOMS_NUMBER))
                .then()
                .statusCode(404);
    }

    @Test
    public void anExistingKeyDateCanBeDeletedByOffenderId() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .delete(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/offenderId/%s/custody/keyDates/POM1",  OFFENDER_ID))
                .then()
                .statusCode(404);
    }
    @Test
    public void anExistingKeyDateCanBeDeletedByBookingNumber() throws JsonProcessingException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterNext = LocalDate.now().plusDays(2);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(tomorrow))
                .when()
                .put(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(dayAfterNext))
                .when()
                .delete(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get(String.format("offenders/prisonBookingNumber/%s/custody/keyDates/POM1", PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldRespond404WhenOffenderNotFound() throws JsonProcessingException {
        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put("offenders/nomsNumber/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .when()
                .delete("offenders/nomsNumber/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/nomsNumber/DOESNOTEXIST/custody/keyDates")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put("offenders/crn/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .when()
                .delete("offenders/crn/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/crn/DOESNOTEXIST/custody/keyDates")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put("offenders/offenderId/999999999/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/offenderId/999999999/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/offenderId/999999999/custody/keyDates")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put("offenders/prisonBookingNumber/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .when()
                .delete("offenders/prisonBookingNumber/DOESNOTEXIST/custody/keyDates/POM1")
                .then()
                .statusCode(404);

        given()
                .header("Authorization", aValidToken())
                .when()
                .get("offenders/prisonBookingNumber/DOESNOTEXIST/custody/keyDates")
                .then()
                .statusCode(404);


    }

    @Test
    public void shouldRespond400WhenAddingKeyDateThatIsNotValid() throws JsonProcessingException {
        JsonPath  errorMessage = given()
                .header("Authorization", aValidToken())
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
    public void shouldRespond400WhenOffenderHasInvalidActiveCustodyEvents() throws JsonProcessingException {
        JsonPath errorMessage = given()
                .header("Authorization", aValidToken())
                .contentType("application/json")
                .body(createCustodyKeyDateOf(LocalDate.now()))
                .when()
                .put(String.format("offenders/crn/%s/custody/keyDates/POM1",  CRN_NO_EVENTS))
                .then()
                .statusCode(400)
                .extract()
                .body()
                .jsonPath();

        assertThat(errorMessage.getString("message")).isEqualTo(String.format("Can only add a key date where offender has one active custody related event. %s has 0", OFFENDER_ID_NO_EVENTS));
    }

        @Override
    public Jwt jwt() {
        return jwt;
    }
}

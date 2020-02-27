package uk.gov.justice.digital.delius.integration.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.data.api.CreateCustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;
import uk.gov.justice.digital.delius.jwt.JwtAuthenticationHelper;

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
    }

    @Test
    public void anAddedKeyDateCanBeRetrievedByCRN() throws JsonProcessingException {
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
    public void anAddedKeyDateCanBeRetrievedByOffenderId() throws JsonProcessingException {
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
    public void anAddedKeyDateCanBeRetrievedByBookingNumber() throws JsonProcessingException {
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
    public void allKeyDatesCanBeRetrievedForAnOffender() throws JsonProcessingException {
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
    public void anExistingKeyDateCanBeUpdated() throws JsonProcessingException {
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
    public void anExistingKeyDateCanBeDeletedByCRN() throws JsonProcessingException {
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
    public void anExistingKeyDateCanBeDeletedByNOMSNumber() throws JsonProcessingException {
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
    public void anExistingKeyDateCanBeDeletedByOffenderId() throws JsonProcessingException {
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
    public void anExistingKeyDateCanBeDeletedByBookingNumber() throws JsonProcessingException {
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
    public void shouldRespond404WhenOffenderNotFound() throws JsonProcessingException {
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
    public void shouldRespond400WhenAddingKeyDateThatIsNotValid() throws JsonProcessingException {
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
    public void shouldRespond400WhenOffenderHasInvalidActiveCustodyEvents() throws JsonProcessingException {
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

        private String createReplaceCustodyKeyDates() {
            try {
                return objectMapper.writeValueAsString(ReplaceCustodyKeyDates
                        .builder()
                        .sentenceExpiryDate(LocalDate.now())
                        .build());
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
}

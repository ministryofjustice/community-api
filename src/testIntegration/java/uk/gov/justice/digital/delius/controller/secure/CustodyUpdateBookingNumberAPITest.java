package uk.gov.justice.digital.delius.controller.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.JwtAuthenticationHelper;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.UpdateCustodyBookingNumber;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("dev-seed")
@DirtiesContext
public class CustodyUpdateBookingNumberAPITest {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String OFFENDER_ID = "2500343964";

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Flyway flyway;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @MockBean
    private TelemetryClient telemetryClient;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
        //noinspection SqlWithoutWhere
        jdbcTemplate.execute("DELETE FROM CONTACT");
    }

    @AfterEach
    public void after() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void updateBookingNumber() throws JsonProcessingException {
        final var token = createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE", "ROLE_COMMUNITY");

        final var custody = given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateCustodyBookingNumber("V74999", LocalDate.of(2019, 9, 5)))
                .when()
                .put(String.format("offenders/nomsNumber/%s/custody/bookingNumber", NOMS_NUMBER))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Custody.class);


        // booking number should be updated on event
        assertThat(custody.getBookingNumber()).isEqualTo("V74999");
        verify(telemetryClient).trackEvent(eq("P2PImprisonmentStatusBookingNumberUpdated"), any(), isNull());

        // booking number should also be updated on Offender
        final var offender = given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nomsNumber/G9542VP")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(OffenderDetailSummary.class);
        assertThat(offender.getOtherIds().getMostRecentPrisonerNumber()).isEqualTo("V74999");

        // contact should be created with booking number
        final var contact = jdbcTemplate.query(
                "SELECT * from CONTACT where OFFENDER_ID = ?",
                List.of(OFFENDER_ID).toArray(),
                new ColumnMapRowMapper())
                .stream()
                .filter(record -> toLocalDateTime(record.get("CONTACT_DATE")).toLocalDate().equals(LocalDate.now()))
                .filter(record -> record.get("EVENT_ID") != null)
                .findFirst()
                .orElseThrow();

        assertThat(contact.get("NOTES").toString()).contains("Prison Number: V74999");

        // offender prisoner record should be created with booking number
        final var offenderPrisoner = jdbcTemplate.query(
                "SELECT * FROM OFFENDER_PRISONER where OFFENDER_ID = ?",
                List.of(OFFENDER_ID).toArray(),
                new ColumnMapRowMapper())
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(offenderPrisoner.get("PRISONER_NUMBER")).isEqualTo("V74999");


    }

    private String createUpdateCustodyBookingNumber(String bookingNumber, LocalDate sentenceStartDate) throws JsonProcessingException {
        return objectMapper.writeValueAsString(UpdateCustodyBookingNumber
                .builder()
                .bookingNumber(bookingNumber)
                .sentenceStartDate(sentenceStartDate)
                .build());
    }
    private String createJwt(final String ...roles ) {
        return jwtAuthenticationHelper.createJwt(JwtAuthenticationHelper.JwtParameters.builder()
                .username("APIUser")
                .roles(List.of(roles))
                .scope(Arrays.asList("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }

    private LocalDateTime toLocalDateTime(Object columnValue) {
        return ((Timestamp)columnValue).toLocalDateTime();
    }


}

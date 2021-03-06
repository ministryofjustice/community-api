package uk.gov.justice.digital.delius.controller.secure;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.digital.delius.FlywayRestoreExtension;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.UpdateCustodyBookingNumber;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith({SpringExtension.class, FlywayRestoreExtension.class})
@ActiveProfiles("dev-seed")
public class CustodyUpdateBookingNumberAPITest extends IntegrationTestBase {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String OFFENDER_ID = "2500343964";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @SpyBean
    private TelemetryClient telemetryClient;

    @BeforeEach
    public void setup() {
        super.setup();
        //noinspection SqlWithoutWhere
        jdbcTemplate.execute("DELETE FROM CONTACT");
    }

    @Test
    public void updateBookingNumber() {
        final var token = createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE", "ROLE_COMMUNITY");

        final var custody = given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateCustodyBookingNumber("V74999", LocalDate.of(2019, 9, 21)))
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

    private String createUpdateCustodyBookingNumber(String bookingNumber, LocalDate sentenceStartDate) {
        return writeValueAsString(UpdateCustodyBookingNumber
                .builder()
                .bookingNumber(bookingNumber)
                .sentenceStartDate(sentenceStartDate)
                .build());
    }
    private LocalDateTime toLocalDateTime(Object columnValue) {
        return ((Timestamp)columnValue).toLocalDateTime();
    }


}

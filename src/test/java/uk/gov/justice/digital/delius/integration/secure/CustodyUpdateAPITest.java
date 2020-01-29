package uk.gov.justice.digital.delius.integration.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;
import uk.gov.justice.digital.delius.jwt.JwtAuthenticationHelper;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
@DirtiesContext
public class CustodyUpdateAPITest {
    private static final String NOMS_NUMBER = "G9542VP";
    private static final String OFFENDER_ID = "2500343964";
    private static final String PRISON_BOOKING_NUMBER = "V74111";

    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    @MockBean
    private TelemetryClient telemetryClient;

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @Test
    public void mustHaveUpdateRole() throws JsonProcessingException {
        final var token = createJwt("ROLE_COMMUNITY");

        given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateCustody("MDI"))
                .when()
                .put(String.format("offenders/nomsNumber/%s/custody/bookingNumber/%s",  NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(403);
    }

    @Test
    public void updatePrisonInstitution() throws JsonProcessingException {
        final var token = createJwt("ROLE_COMMUNITY_CUSTODY_UPDATE");

        final var custody = given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createUpdateCustody("MDI"))
                .when()
                .put(String.format("offenders/nomsNumber/%s/custody/bookingNumber/%s", NOMS_NUMBER, PRISON_BOOKING_NUMBER))
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Custody.class);


        assertThat(custody.getInstitution().getNomsPrisonInstitutionCode()).isEqualTo("MDI");
        verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdated"), any(), isNull());

        //custody record should have been updated
        final var custodyRecord = jdbcTemplate.query(
                "SELECT * from CUSTODY where PRISONER_NUMBER = ?",
                List.of(PRISON_BOOKING_NUMBER).toArray(),
                new ColumnMapRowMapper()).get(0);
        assertThat(toLocalDateTime(custodyRecord.get("LAST_UPDATED_DATETIME"))).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));
        assertThat(toLocalDateTime(custodyRecord.get("LOCATION_CHANGE_DATE")).toLocalDate()).isEqualTo(LocalDate.now());

        // at least one custody history record should be inserted
        final var latestCustodyHistoryRecord = jdbcTemplate.query(
                "SELECT * from CUSTODY_HISTORY where OFFENDER_ID = ?",
                List.of(OFFENDER_ID).toArray(),
                new ColumnMapRowMapper()).stream().max(Comparator.comparing(record -> toLocalDateTime(record.get("HISTORICAL_DATE")))).orElseThrow();
        assertThat(toLocalDateTime(latestCustodyHistoryRecord.get("HISTORICAL_DATE")).toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(latestCustodyHistoryRecord.get("DETAIL")).isEqualTo("Moorland (HMP & YOI)");

    }

    private String createUpdateCustody(String prisonCode) throws JsonProcessingException {
        return objectMapper.writeValueAsString(UpdateCustody
                .builder()
                .nomsPrisonInstitutionCode(prisonCode)
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

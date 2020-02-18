package uk.gov.justice.digital.delius.integration.secure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.UpdateCustodyBookingNumber;
import uk.gov.justice.digital.delius.jwt.JwtAuthenticationHelper;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev-seed")
@DirtiesContext
public class CustodyUpdateBookingNumberAPITest {
    private static final String NOMS_NUMBER = "G9542VP";

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

    @Before
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/secure";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> objectMapper));
    }

    @After
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


        assertThat(custody.getBookingNumber()).isEqualTo("V74999");
        verify(telemetryClient).trackEvent(eq("P2PImprisonmentStatusBookingNumberUpdated"), any(), isNull());
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

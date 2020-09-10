package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.digital.delius.OffenderDeltaHelper;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffenderUpdatesAPITest extends IntegrationTestBase {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanDatabase() {
        jdbcTemplate.execute("delete from OFFENDER_DELTA");
    }


    @Test
    public void mustHaveCommunityRole() {
        final var token = createJwt("ROLE_BANANAS");

        given()
                .auth().oauth2(token)
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nextUpdate")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("can get next update with date changed")
    public void canGetNextUpdateWithDateChanged() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(9L, LocalDateTime.now(), "CREATED")
                .toBuilder().dateChanged(LocalDateTime.parse("2012-01-31T14:23:12"))
                .build();
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nextUpdate")
                .then()
                .statusCode(200)
                .body("dateChanged", equalTo("2012-01-31T14:23:12"))
                .body("offenderDeltaId", equalTo(9));

    }

    @Test
    @DisplayName("will get the next update and then lock that record so next call can not see it")
    public void willGetAndLockNextUpdate() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(9L, LocalDateTime.now(), "CREATED");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nextUpdate")
                .then()
                .statusCode(200);

        given()
                .auth().oauth2(tokenWithRoleCommunity())
                .contentType(APPLICATION_JSON_VALUE)
                .when()
                .get("/offenders/nextUpdate")
                .then()
                .statusCode(404);

    }
}

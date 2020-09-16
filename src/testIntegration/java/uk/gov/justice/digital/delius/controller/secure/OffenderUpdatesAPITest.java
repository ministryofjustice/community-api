package uk.gov.justice.digital.delius.controller.secure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.digital.delius.OffenderDeltaHelper;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OffenderUpdatesAPITest extends IntegrationTestBase {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanDatabase() {
        jdbcTemplate.execute("delete from OFFENDER_DELTA");
    }

    @Nested
    @DisplayName("/offenders/nextUpdate")
    class NextUpdate {

        @Test
        @DisplayName("must have `ROLE_COMMUNITY_EVENTS` to access this service")
        public void mustHaveCommunityRole() {
            final var token = createJwt("ROLE_COMMUNITY");

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
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_EVENTS"))
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
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_EVENTS"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nextUpdate")
                    .then()
                    .statusCode(200);

            given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_EVENTS"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nextUpdate")
                    .then()
                    .statusCode(404);

        }

        @Test
        @DisplayName("will get and lock a single unprocessed offender delta record")
        public void willLockSingleUnprocessedUpdate() {
            final var delta = OffenderDeltaHelper.anOffenderDelta(9L, LocalDateTime.now().minusDays(1), "INPROGRESS")
                    .toBuilder().lastUpdatedDateTime(LocalDateTime.now().minusMinutes(60))
                    .build();
            OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

            given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_EVENTS"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nextUpdate")
                    .then()
                    .statusCode(200)
                    .body("offenderDeltaId", equalTo(9));

            given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_EVENTS"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nextUpdate")
                    .then()
                    .statusCode(404);

        }

        @Test
        @DisplayName("will get next new update when there are older unprocessed updates ")
        public void willLockNewUpdateBeforeOlderUnprocessedUpdates() {
            final var deltaLocked = OffenderDeltaHelper.anOffenderDelta(9L, LocalDateTime.now().minusDays(1), "INPROGRESS")
                    .toBuilder().lastUpdatedDateTime(LocalDateTime.now().minusMinutes(60))
                    .build();
            final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED")
                    .toBuilder().lastUpdatedDateTime(LocalDateTime.now().minusMinutes(1))
                    .build();

            OffenderDeltaHelper.insert(List.of(delta, deltaLocked), jdbcTemplate);

            given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_EVENTS"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .get("/offenders/nextUpdate")
                    .then()
                    .statusCode(200)
                    .body("offenderDeltaId", equalTo(10));

        }


    }

    @Nested
    @DisplayName("/offenders/update/{offenderDeltaId}")
    class DeleteUpdate {
        @Test
        @DisplayName("must have `ROLE_COMMUNITY_EVENTS` to access this service")
        public void mustHaveCommunityRole() {
            OffenderDeltaHelper.insert(List.of(OffenderDeltaHelper.anOffenderDelta(99L, LocalDateTime.now(), "INPROGRESS")), jdbcTemplate);

            final var token = createJwt("ROLE_COMMUNITY");

            given()
                    .auth().oauth2(token)
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .delete("/offenders/update/99")
                    .then()
                    .statusCode(403);
        }
        @Test
        @DisplayName("will delete the update when present")
        public void willDeleteTheUpdate() {
            OffenderDeltaHelper.insert(List.of(OffenderDeltaHelper.anOffenderDelta(99L, LocalDateTime.now(), "INPROGRESS")), jdbcTemplate);
            assertThat(hasUpdateFor(99L)).isTrue();

            given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_EVENTS"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .delete("/offenders/update/99")
                    .then()
                    .statusCode(200);
            assertThat(hasUpdateFor(99L)).isFalse();
        }

        @Test
        @DisplayName("will return not found whn the update in not present")
        public void willReturn404WhenNotPresent() {
            assertThat(hasUpdateFor(99L)).isFalse();

            given()
                    .auth().oauth2(createJwt("ROLE_COMMUNITY_EVENTS"))
                    .contentType(APPLICATION_JSON_VALUE)
                    .when()
                    .delete("/offenders/update/99")
                    .then()
                    .statusCode(404);
        }
    }

    private boolean hasUpdateFor(@SuppressWarnings("SameParameterValue") Long offenderDeltaId) {
        var result = jdbcTemplate.query("SELECT * FROM OFFENDER_DELTA WHERE OFFENDER_DELTA_ID = ?", List.of(offenderDeltaId).toArray(), new ColumnMapRowMapper());
        return result.size() > 0;
    }
}

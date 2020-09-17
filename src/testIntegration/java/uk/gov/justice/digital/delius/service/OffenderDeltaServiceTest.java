package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.OffenderDeltaHelper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.justice.digital.delius.service.OffenderDeltaService.IN_PROGRESS_IS_FAILED_AFTER_MINUTES;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-schema")
class OffenderDeltaServiceTest {

    @Autowired
    private OffenderDeltaService offenderDeltaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("delete from OFFENDER_DELTA");
    }

    @Test
    @DisplayName("can lock a single offender update")
    void lockNextUpdate_canLockSingleDelta() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(9L, LocalDateTime.now().minusMinutes(5), "CREATED");
        OffenderDeltaHelper.insert(List.of(expectedDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.lockNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("will lock the oldest offender update")
    void lockNextUpdate_locksLatestDelta() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(2), "CREATED");
        final var anyDelta = OffenderDeltaHelper.anOffenderDelta(11L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(expectedDelta, anyDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.lockNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(expectedDelta.getOffenderDeltaId());
    }

    @Test
    @DisplayName("will ignore older offender updates in the wrong status")
    void lockNextUpdate_ignoresWrongStatus() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED");
        final var anyDelta = OffenderDeltaHelper.anOffenderDelta(11L, LocalDateTime.now().minusMinutes(2), "INPROGRESS");
        OffenderDeltaHelper.insert(List.of(expectedDelta, anyDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.lockNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(expectedDelta.getOffenderDeltaId());
    }

    @Test
    @DisplayName("will update the status of the locked offender update")
    void lockNextUpdate_updatesStatus() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.lockNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(delta.getOffenderDeltaId());
        assertThat(offenderDelta.getStatus()).isEqualTo("INPROGRESS");
    }

    @Test
    @DisplayName("will not return an offender update after it has been locked once")
    void lockNextUpdate_lockedUpdateIsUnavailable() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        offenderDeltaService.lockNextUpdate().orElseThrow();

        assertThat(offenderDeltaService.lockNextUpdate()).isNotPresent();
    }

    @Test
    @DisplayName("will update the last updated date time when an offender update is locked")
    void lockNextUpdate_lastUpdatedIsUpdated() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        final var timeBeforeUpdate = LocalDateTime.now().withNano(0);

        final var originalLastUpdated = delta.getLastUpdatedDateTime();
        final var offenderDelta = offenderDeltaService.lockNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(delta.getOffenderDeltaId());

        final var lastUpdatedDate = toLocalDateTime(jdbcTemplate.query(
                "SELECT * from OFFENDER_DELTA where OFFENDER_DELTA_ID = ?",
                List.of(10L).toArray(),
                new ColumnMapRowMapper())
                .stream()
                .findFirst()
                .orElseThrow().get("LAST_UPDATED_DATETIME"));
        assertThat(lastUpdatedDate).isAfterOrEqualTo(timeBeforeUpdate);
        assertThat(lastUpdatedDate).isAfter(originalLastUpdated);

    }

    private LocalDateTime toLocalDateTime(Object columnValue) {
        return ((Timestamp) columnValue).toLocalDateTime();
    }


    @Test
    @Transactional
    @DisplayName("will throw if the offender update has been updated before we lock it")
    void lockNextUpdate_willNotAllowTwoThreadsToUpdateTheSameRecord() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        assertThatThrownBy(() -> {
            TestTransaction.flagForCommit();
            assertThat(offenderDeltaService.lockNextUpdate()).isPresent();
            jdbcTemplate.update("update OFFENDER_DELTA set LAST_UPDATED_DATETIME = ? where OFFENDER_DELTA_ID = ?", LocalDateTime.now().minusDays(5), 10L);
            TestTransaction.end();
        }).isInstanceOf(ConcurrencyFailureException.class);

    }

    @Test
    @DisplayName("Will retrieve the failed update that was updated over 10 minutes ago")
    void lockNextFailedUpdate_canLockSingleDelta() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(9L, LocalDateTime.now().minusMinutes(IN_PROGRESS_IS_FAILED_AFTER_MINUTES + 1), "INPROGRESS");
        OffenderDeltaHelper.insert(List.of(expectedDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.lockNextFailedUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("Will retrieve the oldest failed update")
    void lockNextFailedUpdate_locksLatestDelta() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(IN_PROGRESS_IS_FAILED_AFTER_MINUTES + 2), "INPROGRESS");
        final var anyDelta = OffenderDeltaHelper.anOffenderDelta(11L, LocalDateTime.now().minusMinutes(IN_PROGRESS_IS_FAILED_AFTER_MINUTES + 1), "INPROGRESS");
        OffenderDeltaHelper.insert(List.of(expectedDelta, anyDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.lockNextFailedUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(expectedDelta.getOffenderDeltaId());
    }

    @Test
    @DisplayName("Once a failed update has been retrieved it can't be retrieved again straight away")
    void lockNextFailedUpdate_lockedUpdateIsUnavailable() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(IN_PROGRESS_IS_FAILED_AFTER_MINUTES + 1), "INPROGRESS");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        offenderDeltaService.lockNextFailedUpdate().orElseThrow();

        assertThat(offenderDeltaService.lockNextFailedUpdate()).isNotPresent();
    }

    @Test
    @DisplayName("Last updated is set to current time when retrieving and locking a failed update")
    void lockNextFailedUpdate_lastUpdatedIsUpdated() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(IN_PROGRESS_IS_FAILED_AFTER_MINUTES + 1), "INPROGRESS");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        final var timeBeforeUpdate = LocalDateTime.now().withNano(0);

        final var originalLastUpdated = delta.getLastUpdatedDateTime();
        final var offenderDelta = offenderDeltaService.lockNextFailedUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(delta.getOffenderDeltaId());

        final var lastUpdatedDate = toLocalDateTime(jdbcTemplate.query(
                "SELECT * from OFFENDER_DELTA where OFFENDER_DELTA_ID = ?",
                List.of(10L).toArray(),
                new ColumnMapRowMapper())
                .stream()
                .findFirst()
                .orElseThrow().get("LAST_UPDATED_DATETIME"));
        assertThat(lastUpdatedDate).isAfterOrEqualTo(timeBeforeUpdate);
        assertThat(lastUpdatedDate).isAfter(originalLastUpdated);

    }

    @Test
    @Transactional
    @DisplayName("Two threads can not retrieve and lock a failed record, it wil throw exception after several attempts")
    void lockNextFailedUpdate_willNotAllowTwoThreadsToUpdateTheSameRecord() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(IN_PROGRESS_IS_FAILED_AFTER_MINUTES + 1), "INPROGRESS");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        assertThatThrownBy(() -> {
            TestTransaction.flagForCommit();
            assertThat(offenderDeltaService.lockNextFailedUpdate()).isPresent();
            jdbcTemplate.update("update OFFENDER_DELTA set LAST_UPDATED_DATETIME = ? where OFFENDER_DELTA_ID = ?", LocalDateTime.now().minusDays(5), 10L);
            TestTransaction.end();
        }).isInstanceOf(ConcurrencyFailureException.class);

    }

    @Test
    @DisplayName("Will not retrieve failed records that have last been updated within 10 minutes")
    void willNotRetrieveFailedRecordsLessThan10MinutesSinceLastUpdated() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(IN_PROGRESS_IS_FAILED_AFTER_MINUTES - 1), "INPROGRESS");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        assertThat(offenderDeltaService.lockNextFailedUpdate()).isEmpty();
    }
}

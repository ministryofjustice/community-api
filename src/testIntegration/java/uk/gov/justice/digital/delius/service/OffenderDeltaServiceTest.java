package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-schema")
public class OffenderDeltaServiceTest {

    @Autowired
    private OffenderDeltaService offenderDeltaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanDatabase() {
        jdbcTemplate.execute("delete from OFFENDER_DELTA");
    }

    @Test
    public void canLockSingleDelta() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(9L, LocalDateTime.now().minusMinutes(5), "CREATED");
        OffenderDeltaHelper.insert(List.of(expectedDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.getNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(9L);
    }

    @Test
    public void locksLatestDelta() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(2), "CREATED");
        final var anyDelta = OffenderDeltaHelper.anOffenderDelta(11L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(expectedDelta, anyDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.getNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(expectedDelta.getOffenderDeltaId());
    }

    @Test
    public void ignoresWrongStatus() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED");
        final var anyDelta = OffenderDeltaHelper.anOffenderDelta(11L, LocalDateTime.now().minusMinutes(2), "INPROGRESS");
        OffenderDeltaHelper.insert(List.of(expectedDelta, anyDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.getNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(expectedDelta.getOffenderDeltaId());
    }

    @Test
    public void updatesStatus() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.getNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(delta.getOffenderDeltaId());
        assertThat(offenderDelta.getStatus()).isEqualTo("INPROGRESS");

        assertThat(offenderDeltaService.getNextUpdate()).isNotPresent();
    }

    @Test
    public void lastUpdatedIsUpdated() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        final var timeBeforeUpdate = LocalDateTime.now().withNano(0);

        final var originalLastUpdated = delta.getLastUpdatedDateTime();
        final var offenderDelta = offenderDeltaService.getNextUpdate().orElseThrow();

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
        return ((Timestamp)columnValue).toLocalDateTime();
    }


    @Test
    @Transactional
    public void willNotAllowTwoThreadsToUpdateTheSameRecord() {
        final var delta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(delta), jdbcTemplate);

        assertThatThrownBy(() -> {
            TestTransaction.flagForCommit();
            assertThat(offenderDeltaService.getNextUpdate()).isPresent();
            jdbcTemplate.update("update OFFENDER_DELTA set LAST_UPDATED_DATETIME = ? where OFFENDER_DELTA_ID = ?", LocalDateTime.now().minusDays(5), 10L);
            TestTransaction.end();
        }).isInstanceOf(ConcurrencyFailureException.class);

    }

}

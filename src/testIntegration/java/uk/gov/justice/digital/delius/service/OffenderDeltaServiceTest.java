package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.digital.delius.OffenderDeltaHelper;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev-schema")
@Transactional
public class OffenderDeltaServiceTest {

    @Autowired
    private OffenderDeltaService offenderDeltaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void canLockSingleDelta() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(9L, LocalDateTime.now().minusMinutes(5), "CREATED");
        OffenderDeltaHelper.insert(List.of(expectedDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.lockNext().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(9L);
    }

    @Test
    public void locksLatestDelta() {
        final var expectedDelta = OffenderDeltaHelper.anOffenderDelta(10L, LocalDateTime.now().minusMinutes(2), "CREATED");
        final var anyDelta = OffenderDeltaHelper.anOffenderDelta(11L, LocalDateTime.now().minusMinutes(1), "CREATED");
        OffenderDeltaHelper.insert(List.of(expectedDelta, anyDelta), jdbcTemplate);

        final var offenderDelta = offenderDeltaService.lockNext().orElseThrow();

        assertThat(offenderDelta.getOffenderDeltaId()).isEqualTo(expectedDelta.getOffenderDeltaId());
    }
}

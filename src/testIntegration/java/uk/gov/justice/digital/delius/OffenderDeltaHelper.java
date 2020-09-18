package uk.gov.justice.digital.delius;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDelta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class OffenderDeltaHelper {
    public static void insert(final List<OffenderDelta> deltas, final JdbcTemplate jdbcTemplate) {
        deltas.forEach(
                delta -> jdbcTemplate.update(
                        "INSERT INTO OFFENDER_DELTA(OFFENDER_DELTA_ID, OFFENDER_ID, DATE_CHANGED, ACTION, SOURCE_TABLE, SOURCE_RECORD_ID, STATUS, CREATED_DATETIME, LAST_UPDATED_DATETIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        delta.getOffenderDeltaId(), delta.getOffenderId(), delta.getDateChanged(), delta.getAction(),
                        delta.getSourceTable(), delta.getSourceRecordId(), delta.getStatus(),
                        delta.getCreatedDateTime(), delta.getLastUpdatedDateTime()
                )
        );
    }

    public static List<OffenderDelta> someDeltas(final LocalDateTime now, final Long howMany) {

        return LongStream.rangeClosed(1, howMany).mapToObj(l -> OffenderDelta.builder()
                .offenderDeltaId(1000+l)
                .offenderId(l)
                .dateChanged(now.minusDays(howMany / 2).plusDays(l))
                .action("UPSERT")
                .sourceTable("OFFENDER")
                .sourceRecordId(10000+l)
                .status("CREATED")
                .createdDateTime(now.minusMinutes(10))
                .lastUpdatedDateTime(now.minusMinutes(5))
                .build()).collect(Collectors.toList());
    }

    public static OffenderDelta anOffenderDelta(final Long offenderDeltaId, final LocalDateTime lastUpdatedDateTime, final String status) {
        return OffenderDelta.builder()
                .offenderDeltaId(offenderDeltaId)
                .offenderId(1L)
                .dateChanged(LocalDateTime.now())
                .action("UPSERT")
                .sourceTable("OFFENDER")
                .sourceRecordId(2L)
                .status(status)
                .createdDateTime(lastUpdatedDateTime)
                .lastUpdatedDateTime(lastUpdatedDateTime)
                .build();
    }
}

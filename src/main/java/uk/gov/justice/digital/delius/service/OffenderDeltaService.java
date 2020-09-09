package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.jpa.dao.OffenderDelta;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class OffenderDeltaService {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public OffenderDeltaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OffenderDelta> findAll() {
        return jdbcTemplate.query("SELECT * FROM OFFENDER_DELTA", (resultSet, rowNum) ->
                OffenderDelta.builder()
                        .offenderDeltaId(resultSet.getLong("OFFENDER_DELTA_ID"))
                        .offenderId(resultSet.getLong("OFFENDER_ID"))
                        .dateChanged(resultSet.getTimestamp("DATE_CHANGED").toLocalDateTime())
                        .action(resultSet.getString("ACTION"))
                        .sourceTable(resultSet.getString("SOURCE_TABLE"))
                        .sourceRecordId(resultSet.getLong("SOURCE_RECORD_ID"))
                        .status(resultSet.getString("STATUS"))
                        .createdDateTime(resultSet.getTimestamp("CREATED_DATETIME").toLocalDateTime())
                        .lastUpdatedDateTime(resultSet.getTimestamp("LAST_UPDATED_DATETIME").toLocalDateTime())
                        .build())
                        .stream()
                        .limit(1000) // limit to 1000 without using database specific syntax specific technology
                        .collect(toList());
    }

    @Transactional
    public void deleteBefore(LocalDateTime dateTime) {
        jdbcTemplate.update("DELETE FROM OFFENDER_DELTA WHERE DATE_CHANGED < ?", dateTime);
    }

    public OffenderDelta lockNext() {
        return OffenderDelta.builder().build();
    }
}

package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.jpa.dao.OffenderDelta;

import java.time.LocalDateTime;
import java.util.List;

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
                        .offenderId(resultSet.getLong("OFFENDER_ID"))
                        .dateChanged(resultSet.getTimestamp("DATE_CHANGED").toLocalDateTime())
                        .action(resultSet.getString("ACTION"))
                        .build());
    }

    @Transactional
    public void deleteBefore(LocalDateTime dateTime) {
        jdbcTemplate.update("DELETE FROM OFFENDER_DELTA WHERE DATE_CHANGED < ?", dateTime);
    }

}

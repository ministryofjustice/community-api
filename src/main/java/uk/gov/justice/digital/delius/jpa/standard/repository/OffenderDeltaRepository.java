package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDelta;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OffenderDeltaRepository extends JpaRepository<OffenderDelta, Long> {

    Optional<OffenderDelta> findFirstByStatusAndLastUpdatedDateTimeLessThanEqualOrderByCreatedDateTime(final String status, final LocalDateTime cutOffTime);

    @Query("delete from OffenderDelta offenderDelta where offenderDelta.offenderDeltaId in " +
            "(select duplicate.offenderDeltaId " +
            "   from OffenderDelta original inner join OffenderDelta duplicate " +
            "   on ( " +
            "           original.offenderId = duplicate.offenderId and " +
            "           original.sourceTable = duplicate.sourceTable and " +
            "           original.sourceRecordId = duplicate.sourceRecordId) " +
            "   where " +
            "       original.offenderDeltaId = ?1 and " +
            "       duplicate.offenderDeltaId <> ?1)")
    @Modifying
    int deleteOtherDuplicates(Long offenderDeltaId);
}

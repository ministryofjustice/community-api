package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDelta;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OffenderDeltaRepository extends JpaRepository<OffenderDelta, Long> {

    Optional<OffenderDelta> findFirstByStatusAndLastUpdatedDateTimeLessThanEqualOrderByCreatedDateTime(final String status, final LocalDateTime cutOffTime);
}

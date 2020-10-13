package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;

import java.time.LocalDateTime;
import java.util.List;

public interface CourtAppearanceRepository extends JpaRepository<CourtAppearance, Long>  {
    List<CourtAppearance> findByOffenderId(Long offenderId);

    @Query("SELECT ca from CourtAppearance ca where ca.event.eventId = :eventId and ca.offenderId = :offenderId")
    List<CourtAppearance> findByOffenderIdAndEventId(Long offenderId, Long eventId);

    List<CourtAppearance> findByAppearanceDateGreaterThanEqual(LocalDateTime fromDate);
}


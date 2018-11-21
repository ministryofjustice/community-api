package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;

import java.util.List;
import java.util.Optional;

public interface CourtAppearanceRepository extends JpaRepository<CourtAppearance, Long>  {
    List<CourtAppearance> findByOffenderId(Long offenderId);

    Optional<CourtAppearance> findByEventId(Long convictionId);
}


package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;

import java.time.LocalDateTime;
import java.util.List;

public interface CourtAppearanceRepository extends JpaRepository<CourtAppearance, Long>  {
    List<CourtAppearance> findByOffenderId(Long offenderId);

    List<CourtAppearance> findByAppearanceDateGreaterThanEqual(LocalDateTime fromDate);
}


package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderAccessLimitations;

import java.util.Optional;

@Repository
public interface OffenderAccessLimitationRepository extends JpaRepository<OffenderAccessLimitations, Long> {
    Optional<OffenderAccessLimitations> findByCrn(String crn);
}

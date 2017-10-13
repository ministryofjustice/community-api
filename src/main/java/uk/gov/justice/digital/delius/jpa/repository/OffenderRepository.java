package uk.gov.justice.digital.delius.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.entity.Offender;

import java.util.Optional;

@Repository
public interface OffenderRepository extends JpaRepository<Offender, Long> {
    Optional<Offender> findByOffenderId(Long offenderId);
}

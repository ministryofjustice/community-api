package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;

import java.util.Optional;

public interface OffenceRepository extends JpaRepository<Offence, Long> {
    Optional<Offence> findByCode(String code);
}

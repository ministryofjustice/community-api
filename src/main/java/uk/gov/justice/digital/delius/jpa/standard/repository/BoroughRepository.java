package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Borough;

import java.util.Optional;

public interface BoroughRepository extends JpaRepository<Borough, Long> {
    Optional<Borough> findByCode(String code);
}

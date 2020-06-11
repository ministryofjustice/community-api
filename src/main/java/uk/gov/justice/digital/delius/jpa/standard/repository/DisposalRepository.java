package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;

import java.util.Optional;

public interface DisposalRepository extends JpaRepository<Disposal, Long> {
    Optional<Disposal> findByDisposalId(Long disposalId);
}

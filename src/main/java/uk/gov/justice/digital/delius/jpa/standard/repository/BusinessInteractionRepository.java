package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.BusinessInteraction;

import java.util.Optional;

public interface BusinessInteractionRepository extends JpaRepository<BusinessInteraction, Long> {
    Optional<BusinessInteraction> findByBusinessInteractionCode(String code);
}

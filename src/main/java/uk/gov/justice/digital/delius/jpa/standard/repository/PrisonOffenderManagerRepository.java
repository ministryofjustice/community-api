package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;

public interface PrisonOffenderManagerRepository extends JpaRepository<PrisonOffenderManager, Long> {
}

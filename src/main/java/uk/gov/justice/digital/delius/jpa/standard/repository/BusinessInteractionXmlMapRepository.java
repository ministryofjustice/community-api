package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.BusinessInteractionXmlMap;

import java.util.Optional;

public interface BusinessInteractionXmlMapRepository extends JpaRepository<BusinessInteractionXmlMap, Long> {
    Optional<BusinessInteractionXmlMap> findByBusinessInteractionId(Long id);
}

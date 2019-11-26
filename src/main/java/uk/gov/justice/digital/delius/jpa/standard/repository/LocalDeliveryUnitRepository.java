package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.LocalDeliveryUnit;

import java.util.Optional;

public interface LocalDeliveryUnitRepository extends JpaRepository<LocalDeliveryUnit, Long> {
    Optional<LocalDeliveryUnit> findByCode(String code);
}

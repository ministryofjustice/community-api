package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTier;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTierId;

import java.util.Optional;

@Repository
public interface ManagementTierRepository extends JpaRepository<ManagementTier, ManagementTierId> {

    Optional<ManagementTier> findFirstByIdOffenderIdOrderByIdDateChangedDesc(Long offenderId);
}

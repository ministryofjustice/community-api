package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.ManagementTier;

import java.util.Optional;

@Repository
public interface ManagementTierRepository extends JpaRepository<ManagementTier, Long> {
    Optional<ManagementTier> findFirstByOffenderIdAndSoftDeletedEqualsOrderByDateChangedDesc(@Param("offenderId") Long offenderId, @Param("softDeleted") int softDeleted);
}

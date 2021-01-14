package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OfficeLocation;

import java.util.Optional;

@Repository
public interface OfficeLocationRepository  extends JpaRepository<OfficeLocation, Long>, JpaSpecificationExecutor<OfficeLocation> {
    Optional<OfficeLocation> findByCode(String code);
}

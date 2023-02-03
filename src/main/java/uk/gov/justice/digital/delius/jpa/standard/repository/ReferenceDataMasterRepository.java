package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.ReferenceDataMaster;

import java.util.Optional;

public interface ReferenceDataMasterRepository extends JpaRepository<ReferenceDataMaster, Long> {
}

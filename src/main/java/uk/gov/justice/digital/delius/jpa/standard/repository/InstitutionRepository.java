package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<RInstitution, Long> {
    Optional<RInstitution> findByNomisCdeCode(String nomisCdeCode);
    Optional<RInstitution> findByCode(String code);
    String findCodeByNomisCdeCode(String nomisCdeCode);
}

package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;

public interface ResponsibleOfficerRepository extends JpaRepository<ResponsibleOfficer, Long> {
}

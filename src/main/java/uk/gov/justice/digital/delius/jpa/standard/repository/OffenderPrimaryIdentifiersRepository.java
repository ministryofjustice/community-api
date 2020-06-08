package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderPrimaryIdentifiers;

@Repository
public interface OffenderPrimaryIdentifiersRepository extends JpaRepository<OffenderPrimaryIdentifiers, Long>, JpaSpecificationExecutor<OffenderPrimaryIdentifiers> {
}

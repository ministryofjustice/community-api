package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderPrisoner;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderPrisonerPk;

public interface OffenderPrisonerRepository extends JpaRepository<OffenderPrisoner, OffenderPrisonerPk> {
    void deleteAllByOffenderId(long offenderId);
}

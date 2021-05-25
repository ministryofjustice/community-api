package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocation;

import java.util.Optional;

@Repository
public interface CaseAllocationRepository extends JpaRepository<CaseAllocation, Long> {
    Optional<CaseAllocation> findFirstByOffenderIdAndAllocationDecisionDateNotNullOrderByAllocationDecisionDateDesc(@Param("offenderId") Long offenderId);
}

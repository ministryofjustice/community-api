package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocation;

import java.util.Optional;

@Repository
public interface CaseAllocationRepository extends JpaRepository<CaseAllocation, Long> {
    @Query("select ca from CaseAllocation ca " +
        "join ca.event e " +
        "join e.disposal d " +
        "where ca.offenderId = :offenderId " +
        "and ca.allocationDecisionDate is not null " +
        "and e.activeFlag = true and d.activeFlag = true " +
        "and e.softDeleted = false and d.softDeleted = 0 " +
        "order by ca.allocationDecisionDate desc"
    )
    Optional<CaseAllocation> findLatestDecisionOnActiveEvent(@Param("offenderId") Long offenderId, PageRequest pageRequest);
}

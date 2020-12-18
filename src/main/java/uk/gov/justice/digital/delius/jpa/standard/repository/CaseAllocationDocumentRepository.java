package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocationDocument;

import java.util.List;

public interface CaseAllocationDocumentRepository extends JpaRepository<CaseAllocationDocument, Long> {
    @Query("select document from CaseAllocationDocument document, CaseAllocation entity where document.caseAllocation = entity and document.offenderId = :offenderId and document.softDeleted = false")
    List<CaseAllocationDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}

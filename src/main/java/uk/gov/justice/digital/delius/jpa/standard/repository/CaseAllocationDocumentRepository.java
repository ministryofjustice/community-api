package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CaseAllocationDocument;

import java.util.List;

public interface CaseAllocationDocumentRepository extends JpaRepository<CaseAllocationDocument, Long> {
    List<CaseAllocationDocument> findByOffenderId(Long offenderId);
}

package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.AssessmentDocument;

import java.util.List;

public interface AssessmentDocumentRepository extends JpaRepository<AssessmentDocument, Long> {
    List<AssessmentDocument> findByOffenderId(Long offenderId);
}

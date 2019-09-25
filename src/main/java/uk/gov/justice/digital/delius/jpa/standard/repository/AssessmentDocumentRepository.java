package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.AssessmentDocument;

import java.util.List;

public interface AssessmentDocumentRepository extends JpaRepository<AssessmentDocument, Long> {
    @Query("select document from AssessmentDocument document, Assessment entity where document.assessment = entity and document.offenderId = :offenderId")
    List<AssessmentDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}

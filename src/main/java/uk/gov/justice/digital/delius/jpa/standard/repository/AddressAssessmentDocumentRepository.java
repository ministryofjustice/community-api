package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.AddressAssessmentDocument;

import java.util.List;

public interface AddressAssessmentDocumentRepository extends JpaRepository<AddressAssessmentDocument, Long> {
    @Query("select document from AddressAssessmentDocument document, AddressAssessment entity where document.addressAssessment = entity and document.offenderId = :offenderId")
    List<AddressAssessmentDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}

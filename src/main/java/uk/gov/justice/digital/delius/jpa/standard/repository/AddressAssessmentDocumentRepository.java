package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.AddressAssessmentDocument;

import java.util.List;

public interface AddressAssessmentDocumentRepository extends JpaRepository<AddressAssessmentDocument, Long> {
    List<AddressAssessmentDocument> findByOffenderId(Long offenderId);
}

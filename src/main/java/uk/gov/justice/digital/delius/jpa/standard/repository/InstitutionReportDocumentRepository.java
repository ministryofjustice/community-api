package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReportDocument;

import java.util.List;

public interface InstitutionReportDocumentRepository extends JpaRepository<InstitutionalReportDocument, Long> {
    List<InstitutionalReportDocument> findByOffenderId(Long offenderId);
}

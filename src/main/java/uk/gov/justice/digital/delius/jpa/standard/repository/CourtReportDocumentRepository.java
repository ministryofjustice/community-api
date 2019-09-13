package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;

import java.util.List;

public interface CourtReportDocumentRepository extends JpaRepository<CourtReportDocument, Long> {
    List<CourtReportDocument> findByOffenderId(Long offenderId);
}

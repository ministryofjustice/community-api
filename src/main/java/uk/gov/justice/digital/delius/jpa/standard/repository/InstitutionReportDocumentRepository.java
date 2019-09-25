package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReportDocument;

import java.util.List;

public interface InstitutionReportDocumentRepository extends JpaRepository<InstitutionalReportDocument, Long> {
    @Query("select document from InstitutionalReportDocument document, InstitutionalReport entity where document.institutionalReport = entity and document.offenderId = :offenderId")
    List<InstitutionalReportDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}

package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiDocument;

import java.util.List;

public interface NsiDocumentRepository extends JpaRepository<NsiDocument, Long> {
    @Query("select document from NsiDocument document, Nsi entity where document.nsi = entity and document.offenderId = :offenderId and document.softDeleted = 0")
    List<NsiDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}

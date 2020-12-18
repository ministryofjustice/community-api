package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactDocument;

import java.util.List;

public interface ContactDocumentRepository extends JpaRepository<ContactDocument, Long> {
    @Query("select document from ContactDocument document, Contact entity where document.contact = entity and document.offenderId = :offenderId and document.softDeleted = 0")
    List<ContactDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}

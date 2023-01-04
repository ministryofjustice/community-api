package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.Document.DocumentType;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDocument;

import java.util.List;

public interface OffenderDocumentRepository extends JpaRepository<OffenderDocument, Long> {

    @Query("select document from OffenderDocument document where document.offenderId = :offenderId and document.softDeleted = false and document.documentType = :documentType")
    List<OffenderDocument> findByOffenderId(Long offenderId, DocumentType documentType);

    OffenderDocument findByOffenderIdAndDocumentTypeAndSoftDeletedIsFalse(Long offenderId, DocumentType documentType);
}

package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDocument;

import java.util.List;

public interface OffenderDocumentRepository extends JpaRepository<OffenderDocument, Long> {

    @Query("select document from OffenderDocument document where document.offenderId = :offenderId and document.softDeleted = 0")
    List<OffenderDocument> findByOffenderId(Long offenderId);
}

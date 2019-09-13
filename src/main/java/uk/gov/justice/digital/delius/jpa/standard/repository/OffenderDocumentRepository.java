package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDocument;

import java.util.List;

public interface OffenderDocumentRepository extends JpaRepository<OffenderDocument, Long> {
    List<OffenderDocument> findByOffenderId(Long offenderId);
}

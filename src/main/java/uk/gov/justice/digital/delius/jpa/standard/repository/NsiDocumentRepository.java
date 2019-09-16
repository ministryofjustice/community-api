package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiDocument;

import java.util.List;

public interface NsiDocumentRepository extends JpaRepository<NsiDocument, Long> {
    List<NsiDocument> findByOffenderId(Long offenderId);
}

package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactDocument;

import java.util.List;

public interface ContactDocumentRepository extends JpaRepository<ContactDocument, Long> {
    List<ContactDocument> findByOffenderId(Long offenderId);
}

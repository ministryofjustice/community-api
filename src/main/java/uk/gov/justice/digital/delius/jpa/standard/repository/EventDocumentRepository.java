package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.EventDocument;

import java.util.List;

public interface EventDocumentRepository extends JpaRepository<EventDocument, Long> {
    List<EventDocument> findByOffenderId(Long offenderId);
}

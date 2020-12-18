package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.EventDocument;

import java.util.List;

public interface EventDocumentRepository extends JpaRepository<EventDocument, Long> {
    @Query("select document from EventDocument document, Event entity where document.event = entity and document.offenderId = :offenderId and document.softDeleted = 0")
    List<EventDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}

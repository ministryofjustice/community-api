package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalContactDocument;

import java.util.List;

public interface PersonalContactDocumentRepository extends JpaRepository<PersonalContactDocument, Long> {
    @Query("select document from PersonalContactDocument document, PersonalContact entity where document.personalContact = entity and document.offenderId = :offenderId and document.softDeleted = 0")
    List<PersonalContactDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}

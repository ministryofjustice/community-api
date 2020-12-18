package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstanceDocument;

import java.util.List;

public interface PersonalCircumstanceDocumentRepository extends JpaRepository<PersonalCircumstanceDocument, Long> {
    @Query("select document from PersonalCircumstanceDocument document, PersonalCircumstance entity where document.personalCircumstance = entity and document.offenderId = :offenderId and document.softDeleted = 0")
    List<PersonalCircumstanceDocument> findByOffenderId(@Param("offenderId") Long offenderId);
}

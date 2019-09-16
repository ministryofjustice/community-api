package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.PersonalContactDocument;

import java.util.List;

public interface PersonalContactDocumentRepository extends JpaRepository<PersonalContactDocument, Long> {
    List<PersonalContactDocument> findByOffenderId(Long offenderId);
}

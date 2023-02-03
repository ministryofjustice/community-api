package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;

import java.util.List;
import java.util.Optional;

public interface ContactTypeRepository extends JpaRepository<ContactType, Long> {
    Optional<ContactType> findByCode(String code);
}

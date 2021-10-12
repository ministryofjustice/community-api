package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;

import java.util.List;
import java.util.Optional;

public interface ContactTypeRepository extends JpaRepository<ContactType, Long> {
    Optional<ContactType> findByCode(String code);

    @Query("""
        SELECT DISTINCT type FROM ContactType type
        WHERE type.selectable = true
        AND type.attendanceContact = true
        """)
    List<ContactType> findAllSelectableAppointmentTypes();

    List<ContactType> findAllByContactCategoriesCodeValueInAndSelectableTrue(final List<String> codeValue);

    List<ContactType> findAllBySelectableTrue();
}

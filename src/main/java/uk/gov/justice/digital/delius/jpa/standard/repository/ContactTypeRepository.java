package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.ContactType;

import java.util.List;
import java.util.Optional;

public interface ContactTypeRepository extends JpaRepository<ContactType, Long> {
    Optional<ContactType> findByCode(String code);

    /*
        Category code "AL" refers to the "All" category.
    */
    @Query("SELECT DISTINCT type FROM ContactType type " +
        "INNER JOIN FETCH type.contactCategories category " +
        "WHERE category.codeValue = 'AL' AND type.selectable = 'Y' AND type.scheduleFutureAppointments = 'Y' " +
        "AND type.attendanceContact = true")
    List<ContactType> findAllSelectableAppointmentTypes();
}

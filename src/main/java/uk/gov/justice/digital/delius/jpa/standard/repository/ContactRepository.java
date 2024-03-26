package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

import java.time.LocalDate;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {
    @Query("SELECT contact FROM Contact contact "
        + "WHERE contact.offenderId = :offenderId "
        + "AND contact.event.eventId = :eventId "
        + "AND contact.contactDate <= :contactDate "
        + "AND contact.enforcementContact = true")
    List<Contact> findByOffenderAndEventIdEnforcement(@Param("offenderId") Long offenderId,
                                                    @Param("eventId") Long eventId,
                                                    @Param("contactDate") LocalDate contactDate);

    @Query("SELECT contact FROM Contact contact "
        + "WHERE contact.offenderId = :offenderId "
        + "AND contact.eventId = :eventId "
        + "AND contact.contactDate <= :contactDate "
        + "AND (contact.enforcementContact = true OR contact.contactOutcomeType is not null) "
        + "AND contact.contactType.attendanceContact = true "
        + "AND contact.contactType.nationalStandardsContact = true"
    )
    List<Contact> findByOffenderIdAndEventId(@Param("offenderId") Long offenderId,
                                             @Param("eventId") Long eventId,
                                             @Param("contactDate") LocalDate contactDate);

}

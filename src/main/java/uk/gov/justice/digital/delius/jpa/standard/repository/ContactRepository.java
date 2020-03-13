package uk.gov.justice.digital.delius.jpa.standard.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {

    @Query("SELECT contact FROM Contact contact "
        + "WHERE contact.offenderId = :offenderId "
        + "AND contact.event.eventId = :eventId "
        + "AND contact.contactDate <= :toDate "
        + "AND contact.enforcement = '1'")
    List<Contact> findByOffenderAndEventIdEnforcement(@Param("offenderId") Long offenderId,
                                                    @Param("eventId") Long eventId,
                                                    @Param("toDate") LocalDate toDate);
}

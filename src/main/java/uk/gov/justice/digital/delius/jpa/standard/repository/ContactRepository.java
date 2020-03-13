package uk.gov.justice.digital.delius.jpa.standard.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {

    @Query("select contact from Contact contact where contact.event.eventId = :eventId and contact.contactDate <= :toDate and contact.enforcement = '1'")
    List<Contact> findByEventIdEnforcement(@Param("eventId") Long offenderId, @Param("toDate") LocalDate toDate);
}

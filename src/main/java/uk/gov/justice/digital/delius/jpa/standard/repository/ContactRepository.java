package uk.gov.justice.digital.delius.jpa.standard.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {


    @Override
    @EntityGraph(value = "Contact.summary")
    List<Contact> findAll(Specification<Contact> spec);

    @Override
    @EntityGraph(value = "Contact.summary")
    Page<Contact> findAll(Specification<Contact> spec, Pageable pageable);

    @Query("SELECT contact FROM Contact contact "
        + "WHERE contact.offenderId = :offenderId "
        + "AND contact.event.eventId = :eventId "
        + "AND contact.contactDate <= :contactDate "
        + "AND contact.enforcementContact = true")
    List<Contact> findByOffenderAndEventIdEnforcement(@Param("offenderId") Long offenderId,
                                                    @Param("eventId") Long eventId,
                                                    @Param("contactDate") LocalDate contactDate);

    @Query("SELECT contact FROM Contact contact "
        + "LEFT OUTER JOIN ContactOutcomeType cot ON cot = contact.contactOutcomeType "
        + "WHERE contact.offenderId = :offenderId "
        + "AND contact.event.eventId = :eventId "
        + "AND contact.contactDate <= :contactDate "
        + "AND (contact.enforcementContact = true OR contact.contactOutcomeType != null) "
        + "AND contact.contactType.attendanceContact = true "
        + "AND contact.contactType.nationalStandardsContact = 'Y'"
    )
    List<Contact> findByOffenderAndEventId(@Param("offenderId") Long offenderId,
                                            @Param("eventId") Long eventId,
                                            @Param("contactDate") LocalDate contactDate);


    @Query("SELECT contact FROM Contact contact "
        + "WHERE contact.offenderId = :offenderId "
        + "AND contact.nsi.nsiId = :nsiId "
    )
    List<Contact> findByOffenderAndNsiId(@Param("offenderId") Long offenderId,
                                         @Param("nsiId") Long nsiId);

    /**
     * Get appointment (contact with type.attendanceContact) by offender id & contact id.
     * Specifying both offender & contact ids effectively validates that the appointment is associated to the offender.
     */
    Optional<Contact> findByContactIdAndOffenderIdAndContactTypeAttendanceContactIsTrueAndSoftDeletedIsFalse(Long contactId, Long offenderId);

    Optional<Contact> findByContactIdAndOffenderIdAndSoftDeletedIsFalse(Long contactId, Long offenderId);

    @Query("""
           SELECT contact FROM Contact contact
            INNER JOIN ContactType ct ON ct = contact.contactType AND ct.code = :contactType
            WHERE contact.offenderId = :offenderId
            AND contact.nsi.nsiId = :nsiId
            AND contact.contactDate = :contactDate
            AND contact.softDeleted = false"""
    )
    List<Contact> findByOffenderAndNsiIdAndContactTypeAndContactDateAndSoftDeletedIsFalse(
        @Param("offenderId") Long offenderId,
        @Param("nsiId") Long nsiId,
        @Param("contactType") String contactType,
        @Param("contactDate") LocalDate contactDate);
}

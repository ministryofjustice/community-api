package uk.gov.justice.digital.delius.jpa.standard.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {

    @Query("SELECT contact FROM Contact contact "
        + "WHERE contact.offenderId = :offenderId "
        + "AND contact.event.eventId = :eventId "
        + "AND contact.contactDate <= :contactDate "
        + "AND contact.enforcement = '1'")
    List<Contact> findByOffenderAndEventIdEnforcement(@Param("offenderId") Long offenderId,
                                                    @Param("eventId") Long eventId,
                                                    @Param("contactDate") LocalDate contactDate);

    @Query("SELECT contact FROM Contact contact "
        + "LEFT OUTER JOIN ContactOutcomeType cot ON cot = contact.contactOutcomeType "
        + "WHERE contact.offenderId = :offenderId "
        + "AND contact.event.eventId = :eventId "
        + "AND contact.contactDate <= :contactDate "
        + "AND (contact.enforcement = '1' OR contact.contactOutcomeType != null) "
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
}

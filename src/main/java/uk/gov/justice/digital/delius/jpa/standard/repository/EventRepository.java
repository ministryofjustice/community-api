package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOffenderId(Long offenderId);

    @Query("select event from Event event where event.offenderId = :offenderId and event.activeFlag = 1")
    List<Event> findByOffenderIdAndActiveTrue(Long offenderId);

    @Query("select event from Event event join DISPOSAL disposal on disposal.event = event join CUSTODY custody on custody.disposal = disposal where custody.prisonerNumber = :prisonBookingNumber")
    List<Event> findByPrisonBookingNumber(@Param("prisonBookingNumber") String prisonBookingNumber);
    @Query("select event from Event event join DISPOSAL disposal on disposal.event = event join CUSTODY custody on custody.disposal = disposal where event.offenderId  = :offenderId and event.activeFlag = 1 and event.softDeleted = 0")
    List<Event> findActiveByOffenderIdWithCustody(@Param("offenderId") Long offenderId);
}

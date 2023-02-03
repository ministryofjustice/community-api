package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventIdAndOffenderIdAndSoftDeletedFalse(Long eventId, Long offenderId);

    List<Event> findByOffenderId(Long offenderId);

    List<Event> findByOffenderIdAndActiveFlagTrue(Long offenderId);
    Optional<Event> findByOffenderIdAndEventIdAndActiveFlagTrue(Long offenderId, Long eventId);
    @Query("select event from Event event join DISPOSAL disposal on disposal.event = event join CUSTODY custody on custody.disposal = disposal where event.offenderId  = :offenderId and event.activeFlag = true and event.softDeleted = false")
    List<Event> findActiveByOffenderIdWithCustody(@Param("offenderId") Long offenderId);
}

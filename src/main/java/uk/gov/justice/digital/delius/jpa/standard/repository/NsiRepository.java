package uk.gov.justice.digital.delius.jpa.standard.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Nsi;

public interface NsiRepository extends JpaRepository<Nsi, Long> {

    // There is no EVENT_ID on NSI
    @Query("select n from Nsi n where n.event.eventId = :eventId AND n.offenderId = :offenderId")
    List<Nsi> findByEventIdAndOffenderId(@Param("eventId") Long eventId, @Param("offenderId") Long offenderId);
}

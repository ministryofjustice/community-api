package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;

import java.util.Optional;

public interface DisposalRepository extends JpaRepository<Disposal, Long> {
    @Query("select d from DISPOSAL d where d.DISPOSAL_ID=:disposalId AND d.EVENT_ID=:eventId")
    Optional<Disposal> find(@Param("offenderId") Long offenderId, @Param("eventId") Long eventId, @Param("disposalId") Long disposalId);
}

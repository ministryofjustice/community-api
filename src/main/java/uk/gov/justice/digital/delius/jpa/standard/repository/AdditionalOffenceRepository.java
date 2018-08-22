package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;

import java.math.BigDecimal;
import java.util.List;

public interface AdditionalOffenceRepository extends JpaRepository<AdditionalOffence, Long> {
    List<AdditionalOffence> findByEventId(Long eventId);

    @Query(value = "SELECT ADDITIONAL_OFFENCE_ID FROM ADDITIONAL_OFFENCE where EVENT_ID = ?1", nativeQuery = true)
    List<BigDecimal> listOffenceIdsForEvent(Long eventId);

}

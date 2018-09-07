package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;

import java.util.List;

public interface AdditionalOffenceRepository extends JpaRepository<AdditionalOffence, Long> {
    List<AdditionalOffence> findByEventId(Long eventId);
}

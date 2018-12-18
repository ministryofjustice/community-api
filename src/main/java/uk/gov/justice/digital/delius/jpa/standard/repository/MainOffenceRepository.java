package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;

import java.util.List;

public interface MainOffenceRepository extends JpaRepository<MainOffence, Long> {
    List<MainOffence> findByOffenderId(Long offenderId);
}

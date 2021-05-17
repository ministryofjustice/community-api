package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;

import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByCode(String code);
}

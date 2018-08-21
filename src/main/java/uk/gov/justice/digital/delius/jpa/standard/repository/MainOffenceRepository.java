package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;

import java.math.BigDecimal;
import java.util.List;

public interface MainOffenceRepository extends JpaRepository<MainOffence, Long> {
    List<MainOffence> findByOffenderId(Long offenderId);

    @Query(value = "SELECT MAIN_OFFENCE_ID FROM MAIN_OFFENCE where OFFENDER_ID = ?1", nativeQuery = true)
    List<BigDecimal> listOffenceIdsForOffender(Long offenderId);

}

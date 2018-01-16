package uk.gov.justice.digital.delius.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.entity.Offender;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OffenderRepository extends JpaRepository<Offender, Long> {
    Optional<Offender> findByOffenderId(Long offenderId);
    Optional<Offender> findByCrn(String crn);
    Optional<Offender> findByNomsNumber(String nomsNumber);

    @Query(value = "SELECT offender_id FROM offender", nativeQuery=true)
    List<BigDecimal> listOffenderIds();
}

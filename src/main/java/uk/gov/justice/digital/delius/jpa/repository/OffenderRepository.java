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

    @Query(value = "SELECT offender_id FROM (SELECT offender_id, ROW_NUMBER() OVER (ORDER BY offender_id) row_num FROM offender) where row_num >= ?1 and row_num <= ?2", nativeQuery=true)
    List<BigDecimal> listOffenderIds(int lower, int upper);
}

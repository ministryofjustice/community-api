package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OffenderRepository extends JpaRepository<Offender, Long>, JpaSpecificationExecutor<Offender> {
    Optional<Offender> findByOffenderId(Long offenderId);

    Optional<Offender> findByCrn(String crn);

    @Query("select o from Offender o where o.softDeleted = 0 and o.nomsNumber = :nomsNumber")
    Optional<Offender> findByNomsNumber(@Param("nomsNumber") String nomsNumber);

    // there are a small number of offenders (100 as of April 2020) that have duplicate NOMS numbers
    // this allows features that can deal with duplicates to access all offenders with the same number
    @Query("select o from Offender o where o.softDeleted = 0 and o.nomsNumber = :nomsNumber")
    List<Offender> findAllByNomsNumber(@Param("nomsNumber") String nomsNumber);

    @Query(value = "SELECT OFFENDER_ID FROM (SELECT QRY_PAG.*, ROWNUM rnum FROM (SELECT OFFENDER_ID FROM OFFENDER) QRY_PAG WHERE ROWNUM <= ?2) WHERE rnum >= ?1", nativeQuery = true)
    List<BigDecimal> listOffenderIds(int lower, int upper);
}

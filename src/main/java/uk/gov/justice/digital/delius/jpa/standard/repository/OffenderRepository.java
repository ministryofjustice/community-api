package uk.gov.justice.digital.delius.jpa.standard.repository;

import io.vavr.control.Either;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
public interface OffenderRepository extends JpaRepository<Offender, Long>, JpaSpecificationExecutor<Offender> {

    Optional<Offender> findByCrnAndSoftDeletedFalse(String crn);

    class DuplicateOffenderException extends RuntimeException {
        public DuplicateOffenderException(String message) {
            super(message);
        }
    }
    Optional<Offender> findByOffenderId(Long offenderId);

    Optional<Offender> findByCrn(String crn);

    @Query("select o from Offender o where o.softDeleted = false and upper(o.nomsNumber) = upper(:nomsNumber)")
    Optional<Offender> findByNomsNumber(@Param("nomsNumber") String nomsNumber);

    // there are a small number of offenders (100 as of April 2020) that have duplicate NOMS numbers
    // this allows features that can deal with duplicates to access all offenders with the same number
    @Query("select o from Offender o where o.softDeleted = false and upper(o.nomsNumber) = upper(:nomsNumber)")
    List<Offender> findAllByNomsNumber(@Param("nomsNumber") String nomsNumber);

    @Query("select o.offenderId from Offender o where o.crn = :crn")
    Optional<Long> getOffenderIdFrom(@Param("crn") String crn);

    default Either<DuplicateOffenderException, Optional<Offender>> findMostLikelyByNomsNumber(String nomsNumber) {
        final var offenders = findAllByNomsNumber(nomsNumber);
        switch (offenders.size()) {
            case 0:
                return Either.right(Optional.empty());
            case 1:
                return Either.right(Optional.of(offenders.getFirst()));
            default: {
                final var activeOffenders = offenders.stream().filter(Offender::hasActiveSentence).toList();
                if (activeOffenders.size() == 1) {
                    return Either.right(Optional.of(activeOffenders.getFirst()));
                } else {
                    return Either.left(new DuplicateOffenderException(String.format("Expecting only a single active offender but found %d offenders with noms number %s", offenders.size(), nomsNumber)));
                }
            }
        }
    }

}

package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderDelta;

import java.util.Optional;

@Repository
public interface OffenderDeltaRepository extends JpaRepository<OffenderDelta, Long> {

    Optional<OffenderDelta> findFirstByStatusOrderByCreatedDateTime(final String status);

//    @Modifying
//    @Query("update OFFENDER_DELTA set STATUS = \"IN_PROGRESS\" \n" +
//            "where STATUS = \"CREATED\" and OFFENDER_DELTA_ID = \n" +
//            "     (SELECT OFFENDER_DELTA_ID WHERE OFFENDER_DELTA_ID = min(OFFENDER_DELTA_ID) where STATUS = \"CREATED\" )")
//    public int lockNext()
}

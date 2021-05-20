package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;

import java.util.Optional;

@Repository
public interface OGRSAssessmentRepository extends JpaRepository<OGRSAssessment, Long> {

    Optional<OGRSAssessment> findFirstByEventOffenderIdAndSoftDeletedEqualsAndEventSoftDeletedEqualsOrderByAssessmentDateDescLastUpdatedDateDesc(@Param("offenderId") Long offenderId, @Param("softDeleted") int softDeleted, @Param("eventSoftDeleted") boolean eventSoftDeleted);

}

package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;

import java.util.Optional;

@Repository
public interface OASYSAssessmentRepository extends JpaRepository<OASYSAssessment, Long> {

    Optional<OASYSAssessment> findFirstByOffenderIdAndSoftDeletedEqualsOrderByAssessmentDateDescLastUpdatedDateDesc(@Param("offenderId") Long offenderId, @Param("softDeleted") int softDeleted);

}

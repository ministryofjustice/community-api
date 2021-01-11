package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OASYSAssessment;

import java.util.Optional;

@Repository
public interface OASYSAssessmentRepository extends JpaRepository<OASYSAssessment, Long> {

    @Query("select assessment from OASYSAssessment assessment " +
        "where assessment.softDeleted = 0 " +
        "and assessment.offenderId = :offenderId " +
        "and assessment.lastUpdatedDate = \n" +
        "    (select max(assessmentLatest.lastUpdatedDate) \n" +
        "    from OASYSAssessment assessmentLatest \n" +
        "    where assessmentLatest.offenderId = :offenderId \n" +
        "    and assessmentLatest.softDeleted = 0)")
    Optional<OASYSAssessment> findLatestByOffenderId(@Param("offenderId") Long offenderId);

}

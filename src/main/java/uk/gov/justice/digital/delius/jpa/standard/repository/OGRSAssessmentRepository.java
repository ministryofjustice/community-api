package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.delius.jpa.standard.entity.OGRSAssessment;

@Repository
public interface OGRSAssessmentRepository extends JpaRepository<OGRSAssessment, Long> {

    @Query("select assessment from OGRSAssessment assessment inner join assessment.event event where event.offenderId = :offenderId and assessment.softDeleted = 0 and assessment.lastUpdatedDate = \n" +
        "    (select max(assessmentLatest.lastUpdatedDate) \n" +
        "    from OGRSAssessment assessmentLatest \n" +
        "    inner join assessmentLatest.event eventLatest \n" +
        "    where eventLatest.offenderId = :offenderId \n" +
        "    and assessmentLatest.softDeleted = 0)")
    OGRSAssessment findLatestByOffenderId(@Param("offenderId") Long offenderId);

}

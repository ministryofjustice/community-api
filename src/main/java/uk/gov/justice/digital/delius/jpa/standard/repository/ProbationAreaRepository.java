package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import java.util.List;
import java.util.Optional;

public interface ProbationAreaRepository extends JpaRepository<ProbationArea, Long>, JpaSpecificationExecutor<ProbationArea> {
    Optional<ProbationArea> findByCode(String code);

    @Query("select pa from ProbationArea pa, RInstitution institution where pa.institution = institution and institution.nomisCdeCode = :nomisCdeCode")
    Optional<ProbationArea> findByInstitutionByNomsCDECode(@Param("nomisCdeCode") String nomsPrisonInstitutionCode);

    Optional<ProbationArea> findByInstitutionInstitutionId(Long institutionId);
}

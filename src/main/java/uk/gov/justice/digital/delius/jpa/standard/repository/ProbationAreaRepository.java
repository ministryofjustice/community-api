package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import java.util.Optional;

public interface ProbationAreaRepository extends JpaRepository<ProbationArea, Long>, JpaSpecificationExecutor<ProbationArea> {
    Optional<ProbationArea> findByCode(String code);
}

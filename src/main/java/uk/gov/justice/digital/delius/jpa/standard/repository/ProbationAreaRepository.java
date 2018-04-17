package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

public interface ProbationAreaRepository extends JpaRepository<ProbationArea, Long>, JpaSpecificationExecutor<ProbationArea> {
}

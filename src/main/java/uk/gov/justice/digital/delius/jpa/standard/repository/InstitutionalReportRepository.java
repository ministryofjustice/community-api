package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport;

import java.util.List;
import java.util.Optional;

public interface InstitutionalReportRepository extends JpaRepository<InstitutionalReport, Long> {

    Optional<InstitutionalReport> findByOffenderIdAndInstitutionalReportId(Long offenderId, Long institutionalReportId);
}

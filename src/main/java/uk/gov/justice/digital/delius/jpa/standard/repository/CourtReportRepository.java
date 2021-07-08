package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport;

import java.util.List;
import java.util.Optional;

public interface CourtReportRepository extends JpaRepository<CourtReport, Long> {
    List<CourtReport> findByOffenderId(Long offenderId);

    Optional<CourtReport> findByOffenderIdAndCourtReportIdAndSoftDeletedFalse(Long offenderId, Long courtReportId);
}

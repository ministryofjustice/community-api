package uk.gov.justice.digital.delius.jpa.standard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport;

import java.util.List;
import java.util.Optional;

public interface CourtReportRepository extends JpaRepository<CourtReport, Long> {
    List<CourtReport> findByOffenderId(Long offenderId);

    Optional<CourtReport> findByOffenderIdAndCourtReportIdAndSoftDeletedFalse(Long offenderId, Long courtReportId);

    @Query("select courtReport from CourtReport courtReport where courtReport.offenderId = :offenderId and courtReport.courtAppearance.event.eventId = :eventId"
        + " and courtReport.softDeleted = false")
    List<CourtReport> findByOffenderIdAndEventId(Long offenderId, Long eventId);
}

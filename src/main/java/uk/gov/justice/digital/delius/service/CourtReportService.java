package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtReportRepository;
import uk.gov.justice.digital.delius.transformers.CourtReportTransformer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
public class CourtReportService {

    private final CourtReportRepository courtReportRepository;
    private final CourtReportTransformer courtReportTransformer;

    @Autowired
    public CourtReportService(CourtReportRepository courtReportRepository,
                              CourtReportTransformer courtAppearanceTransformer) {

        this.courtReportRepository = courtReportRepository;
        this.courtReportTransformer = courtAppearanceTransformer;
    }

    public List<CourtReport> courtReportsFor(Long offenderId) {

        List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport> courtReports = courtReportRepository.findByOffenderId(offenderId);
        return courtReports
            .stream()
            .filter(this::notDeleted)
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport::getDateRequested).reversed())
            .map(CourtReportTransformer::courtReportOf)
            .collect(toList());
    }

    public Optional<CourtReport> courtReportFor(Long offenderId, Long courtReportId) {

        Optional<uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport> maybeCourtReport = courtReportRepository.findByOffenderIdAndCourtReportId(offenderId, courtReportId);
        return maybeCourtReport
                .filter(this::notDeleted)
                .map(CourtReportTransformer::courtReportOf);
    }

    private boolean notDeleted(uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport courtReport) {
        return !convertToBoolean(courtReport.getSoftDeleted());
    }

}

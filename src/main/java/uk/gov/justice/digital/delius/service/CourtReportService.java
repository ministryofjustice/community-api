package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.data.api.CourtReportMinimal;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtReportRepository;
import uk.gov.justice.digital.delius.transformers.CourtReportTransformer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class CourtReportService {

    private final CourtReportRepository courtReportRepository;

    @Autowired
    public CourtReportService(CourtReportRepository courtReportRepository) {

        this.courtReportRepository = courtReportRepository;
    }

    public List<CourtReport> courtReportsFor(Long offenderId) {

        List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport> courtReports = courtReportRepository.findByOffenderId(offenderId);
        return courtReports
            .stream()
            .filter(courtReport -> !courtReport.isSoftDeleted())
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport::getDateRequested).reversed())
            .map(CourtReportTransformer::courtReportOf)
            .collect(toList());
    }

    public Optional<CourtReport> courtReportFor(Long offenderId, Long courtReportId) {

        return courtReportRepository.findByOffenderIdAndCourtReportIdAndSoftDeletedFalse(offenderId, courtReportId)
                .map(CourtReportTransformer::courtReportOf);
    }

    public Optional<CourtReportMinimal> courtReportMinimalFor(Long offenderId, Long courtReportId) {

        return courtReportRepository.findByOffenderIdAndCourtReportIdAndSoftDeletedFalse(offenderId, courtReportId)
            .map(CourtReportTransformer::courtReportMinimalOf);
    }

}

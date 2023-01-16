package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.data.api.CourtReportMinimal;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtReportRepository;
import uk.gov.justice.digital.delius.transformers.CourtReportTransformer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourtReportService {

    private final CourtReportRepository courtReportRepository;

    @Autowired
    public CourtReportService(CourtReportRepository courtReportRepository) {

        this.courtReportRepository = courtReportRepository;
    }

    public Optional<CourtReport> courtReportFor(Long offenderId, Long courtReportId) {

        return courtReportRepository.findByOffenderIdAndCourtReportIdAndSoftDeletedFalse(offenderId, courtReportId)
                .map(CourtReportTransformer::courtReportOf);
    }

    public Optional<CourtReportMinimal> courtReportMinimalFor(Long offenderId, Long courtReportId) {

        return courtReportRepository.findByOffenderIdAndCourtReportIdAndSoftDeletedFalse(offenderId, courtReportId)
            .map(CourtReportTransformer::courtReportMinimalOf);
    }

    public List<CourtReportMinimal> courtReportsMinimalFor(Long offenderId, Long eventId) {

        return courtReportRepository.findByOffenderIdAndEventId(offenderId, eventId)
            .stream()
            .map(CourtReportTransformer::courtReportMinimalOf)
            .collect(Collectors.toList());
    }
}

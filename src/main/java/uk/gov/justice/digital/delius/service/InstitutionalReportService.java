package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.InstitutionalReport;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionalReportRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.MainOffenceRepository;
import uk.gov.justice.digital.delius.transformers.InstitutionalReportTransformer;
import uk.gov.justice.digital.delius.transformers.MainOffenceTransformer;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
public class InstitutionalReportService {

    private final InstitutionalReportRepository institutionalReportRepository;
    private final InstitutionalReportTransformer institutionalReportTransformer;
    private final OffenceService offenceService;

    @Autowired
    public InstitutionalReportService(InstitutionalReportRepository institutionalReportRepository,
                                      InstitutionalReportTransformer institutionalAppearanceTransformer,
                                      MainOffenceRepository mainOffenceRepository, MainOffenceTransformer mainOffenceTransformer, OffenceService offenceService) {

        this.institutionalReportRepository = institutionalReportRepository;
        this.institutionalReportTransformer = institutionalAppearanceTransformer;
        this.offenceService = offenceService;
    }

    public List<InstitutionalReport> institutionalReportsFor(Long offenderId) {

        List<uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport> institutionalReports =
            institutionalReportRepository.findByOffenderId(offenderId);

        return institutionalReports
            .stream()
            .filter(this::notDeleted)
            .map(institutionalReportTransformer::institutionalReportOf)
            .map(this::updateConvictionWithMainOffence)
            .collect(toList());
    }

    public Optional<InstitutionalReport> institutionalReportFor(Long offenderId, Long institutionalReportId) {

        Optional<uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport> maybeInstitutionalReport =
            institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(offenderId, institutionalReportId);

        return maybeInstitutionalReport
                .filter(this::notDeleted)
            .map(institutionalReportTransformer::institutionalReportOf)
            .map(this::updateConvictionWithMainOffence);
    }

    private InstitutionalReport updateConvictionWithMainOffence(InstitutionalReport institutionalReport) {

        return Optional.ofNullable(institutionalReport.getConviction())
            .map(ignored -> institutionalReport.toBuilder()
                .conviction(
                    institutionalReport.getConviction().toBuilder()
                        .offences(offenceService.eventOffencesFor(institutionalReport.getConviction().getConvictionId()))
                        .build())
                .build())
            .orElseGet(() -> institutionalReport);
    }

    private boolean notDeleted(uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport institutionalReport) {
        return !convertToBoolean(institutionalReport.getSoftDeleted());
    }

}

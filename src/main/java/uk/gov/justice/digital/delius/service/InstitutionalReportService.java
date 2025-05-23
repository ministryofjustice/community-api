package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.InstitutionalReport;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionalReportRepository;
import uk.gov.justice.digital.delius.transformers.InstitutionalReportTransformer;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
public class InstitutionalReportService {

    private final InstitutionalReportRepository institutionalReportRepository;

    @Autowired
    public InstitutionalReportService(InstitutionalReportRepository institutionalReportRepository) {

        this.institutionalReportRepository = institutionalReportRepository;
    }

    public Optional<InstitutionalReport> institutionalReportFor(Long offenderId, Long institutionalReportId) {

        Optional<uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport> maybeInstitutionalReport =
            institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(offenderId, institutionalReportId);

        return maybeInstitutionalReport
                .filter(this::notDeleted)
                .map(InstitutionalReportTransformer::institutionalReportOf);
    }

    private boolean notDeleted(uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport institutionalReport) {
        return !convertToBoolean(institutionalReport.getSoftDeleted());
    }

}

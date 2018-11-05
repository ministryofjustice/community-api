package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.InstitutionalReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;

import java.util.Optional;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class InstitutionalReportTransformer {

    public InstitutionalReport institutionalReportOf(uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport report) {
        return InstitutionalReport.builder()
            .institutionalReportId(report.getInstitutionalReportId())
            .offenderId(report.getOffenderId())
            .conviction(convictionOf(report))
            .build();
    }

    private Conviction convictionOf(uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport report) {

        return Optional.ofNullable(report.getCustody())
            .filter(custody -> !convertToBoolean(custody.getSoftDeleted()))
            .map(Custody::getDisposal)
            .filter(disposal -> !convertToBoolean(disposal.getSoftDeleted()))
            .map(Disposal::getEvent)
            .filter(event -> !convertToBoolean(event.getSoftDeleted()))
            .map(event -> Conviction.builder()
                .active(zeroOneToBoolean(event.getActiveFlag()))
                .convictionDate(event.getConvictionDate())
                .convictionId(event.getEventId())
                .build())
            .orElse(null);
    }

}

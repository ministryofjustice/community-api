package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.InstitutionalReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;

import java.util.Optional;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

public class InstitutionalReportTransformer {

    public static InstitutionalReport institutionalReportOf(uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport report) {
        return InstitutionalReport.builder()
            .institutionalReportId(report.getInstitutionalReportId())
            .offenderId(report.getOffenderId())
            .conviction(convictionOf(report))
            .build();
    }


    private static Conviction convictionOf(uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport report) {

        return Optional.ofNullable(report.getCustody())
            .filter(custody -> !convertToBoolean(custody.getSoftDeleted()))
            .map(Custody::getDisposal)
            .filter(disposal -> !convertToBoolean(disposal.getSoftDeleted()))
            .map(Disposal::getEvent)
            .filter(event -> !event.isSoftDeleted())
            .map(ConvictionTransformer::convictionOf)
            .orElse(null);
    }

}

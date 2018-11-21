package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.InstitutionalReport;
import uk.gov.justice.digital.delius.data.api.Sentence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

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
            .sentence(sentenceOf(report))
            .build();
    }

    private Sentence sentenceOf(uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport report) {
        return Optional.ofNullable(report.getCustody())
            .filter(custody -> !convertToBoolean(custody.getSoftDeleted()))
            .map(Custody::getDisposal)
            .filter(disposal -> !convertToBoolean(disposal.getSoftDeleted()))
            .map(disposal -> Sentence.builder()
                .defaultLength(disposal.getLength())
                .effectiveLength(disposal.getEffectiveLength())
                .lengthInDays(disposal.getLengthInDays())
                .originalLength(disposal.getEntryLength())
                .originalLengthUnits(Optional.ofNullable(disposal.getEntryLengthUnits())
                                        .map(StandardReference::getCodeDescription)
                                        .orElse(null))
                .secondLength(disposal.getLength2())
                .secondLengthUnits(Optional.ofNullable(disposal.getEntryLength2Units())
                                    .map(StandardReference::getCodeDescription)
                                    .orElse(null))
                .build())
            .orElse(null);
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

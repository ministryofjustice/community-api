package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.data.api.CourtReportMinimal;
import uk.gov.justice.digital.delius.data.api.KeyValue;

import java.util.Optional;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

public class CourtReportTransformer {

    public static CourtReport courtReportOf(uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport report) {
        return CourtReport.builder()
                        .courtReportId(report.getCourtReportId())
                        .dateRequested(report.getDateRequested())
                        .dateRequired(report.getDateRequired())
                        .allocationDate(report.getAllocationDate())
                        .completedDate(report.getCompletedDate())
                        .sentToCourtDate(report.getSentToCourtDate())
                        .receivedByCourtDate(report.getReceivedByCourtDate())
                        .videoLink(report.getVideoLink())
                        .notes(report.getNotes())
                        .punishment(ynToBoolean(report.getPunishment()))
                        .reductionOfCrime(ynToBoolean(report.getReductionOfCrime()))
                        .reformAndRehabilitation(ynToBoolean(report.getReformAndRehabilitation()))
                        .publicProtection(ynToBoolean(report.getPublicProtection()))
                        .reparation(ynToBoolean(report.getReparation()))
                        .recommendationsNotStated(ynToBoolean(report.getRecommendationsNotStated()))
                        .levelOfSeriousnessId(report.getLevelOfSeriousnessId())
                        .deliveredReportReasonId(report.getDeliveredReportReasonId())
                        .section178(ynToBoolean(report.getSection178()))
                        .createdDatetime(report.getCreatedDatetime())
                        .lastUpdatedDatetime((report.getLastUpdatedDatetime()))
                        .courtReportTypeId(report.getCourtReportTypeId())
                        .deliveredCourtReportTypeId(report.getDeliveredCourtReportTypeId())
                        .offenderId(report.getOffenderId())
                        .requiredByCourt(Optional.ofNullable(report.getRequiredByCourt()).map(CourtTransformer::courtOf).orElse(null))
                        .pendingTransfer(zeroOneToBoolean(report.getPendingTransfer()))
                        .build();
    }

    public static CourtReportMinimal courtReportMinimalOf(uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport report) {
        return CourtReportMinimal.builder()
            .courtReportId(report.getCourtReportId())
            .offenderId(report.getOffenderId())
            .requestedDate(report.getDateRequested())
            .requiredDate(report.getDateRequired())
            .allocationDate(report.getAllocationDate())
            .completedDate(report.getCompletedDate())
            .sentToCourtDate(report.getSentToCourtDate())
            .receivedByCourtDate(report.getReceivedByCourtDate())
            .courtReportType(Optional.ofNullable(report.getCourtReportType())
                                                    .map(reportType -> KeyValue.builder()
                                                                        .code(reportType.getCode())
                                                                        .description(reportType.getDescription())
                                                                        .build())
                                                    .orElse(null))
            .build();
    }

}

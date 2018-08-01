package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Court;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtReport;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class CourtAppearanceTransformer {

    public List<CourtAppearance> courtAppearancesOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance> courtAppearances) {
        return courtAppearances.stream()
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance::getAppearanceDate).reversed())
            .map(this::courtAppearanceOf)
            .collect(Collectors.toList());
    }

    private CourtAppearance courtAppearanceOf(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return CourtAppearance.builder()
            .courtAppearanceId(courtAppearance.getCourtAppearanceId())
            .appearanceDate(courtAppearance.getAppearanceDate())
            .crownCourtCalendarNumber(courtAppearance.getCrownCourtCalendarNumber())
            .bailConditions(courtAppearance.getBailConditions())
            .courtNotes(courtAppearance.getCourtNotes())
            .eventId(courtAppearance.getEventId())
            .teamId(courtAppearance.getTeamId())
            .staffId(courtAppearance.getStaffId())
            .softDeleted(zeroOneToBoolean(courtAppearance.getSoftDeleted()))
            .court(courtOf(courtAppearance.getCourt()))
            .appearanceTypeId(courtAppearance.getAppearanceTypeId())
            .pleaId(courtAppearance.getPleaId())
            .outcomeId(courtAppearance.getOutcomeId())
            .remandStatusId(courtAppearance.getRemandStatusId())
            .createdDatetime(courtAppearance.getCreatedDatetime())
            .lastUpdatedDatetime(courtAppearance.getLastUpdatedDatetime())
            .offenderId(courtAppearance.getOffenderId())
            .courtReports(courtReportsOf(courtAppearance.getCourtReports()))
            .build();
    }

    private List<CourtReport> courtReportsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport> courtReports) {
        return courtReports.stream()
            .map(report -> CourtReport.builder()
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
                .softDeleted(zeroOneToBoolean(report.getSoftDeleted()))
                .levelOfSeriousnessId(report.getLevelOfSeriousnessId())
                .deliveredReportReasonId(report.getDeliveredReportReasonId())
                .section178(ynToBoolean(report.getSection178()))
                .createdDatetime(report.getCreatedDatetime())
                .lastUpdatedDatetime((report.getLastUpdatedDatetime()))
                .courtReportTypeId(report.getCourtReportTypeId())
                .deliveredCourtReportTypeId(report.getDeliveredCourtReportTypeId())
                .offenderId(report.getOffenderId())
                .requiredByCourtId(report.getRequiredByCourtId())
                .pendingTransfer(zeroOneToBoolean(report.getPendingTransfer()))
                .build())
            .collect(Collectors.toList());
    }

    private Court courtOf(uk.gov.justice.digital.delius.jpa.standard.entity.Court court) {
        return Court.builder()
            .courtId(court.getCourtId())
            .code(court.getCode())
            .selectable(ynToBoolean(court.getSelectable()))
            .courtName(court.getCourtName())
            .telephoneNumber(court.getTelephoneNumber())
            .fax(court.getFax())
            .buildingName(court.getBuildingName())
            .street(court.getStreet())
            .locality(court.getLocality())
            .town(court.getTown())
            .county(court.getCounty())
            .postcode(court.getPostcode())
            .country(court.getCountry())
            .courtTypeId(court.getCourtTypeId())
            .createdDatetime(court.getCreatedDatetime())
            .lastUpdatedDatetime(court.getLastUpdatedDatetime())
            .probationAreaId(court.getProbationAreaId())
            .secureEmailAddress(court.getSecureEmailAddress())
            .build();
    }

}

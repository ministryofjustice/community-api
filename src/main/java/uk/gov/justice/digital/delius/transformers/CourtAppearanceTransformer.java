package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class CourtAppearanceTransformer {

    public static CourtAppearance courtAppearanceOf(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return CourtAppearance.builder()
            .courtAppearanceId(courtAppearance.getCourtAppearanceId())
            .appearanceDate(courtAppearance.getAppearanceDate())
            .crownCourtCalendarNumber(courtAppearance.getCrownCourtCalendarNumber())
            .bailConditions(courtAppearance.getBailConditions())
            .courtNotes(courtAppearance.getCourtNotes())
            .eventId(courtAppearance.getEvent().getEventId())
            .teamId(courtAppearance.getTeamId())
            .staffId(courtAppearance.getStaffId())
            .court(CourtTransformer.courtOf(courtAppearance.getCourt()))
            .appearanceTypeId(courtAppearance.getAppearanceTypeId())
            .pleaId(courtAppearance.getPleaId())
            .outcome(CourtAppearanceTransformer.outcomeOf(courtAppearance.getOutcome()))
            .remandStatusId(courtAppearance.getRemandStatusId())
            .createdDatetime(courtAppearance.getCreatedDatetime())
            .lastUpdatedDatetime(courtAppearance.getLastUpdatedDatetime())
            .offenderId(courtAppearance.getOffenderId())
            .courtReports(CourtAppearanceTransformer.courtReportsOf(courtAppearance.getCourtReports()))
            .build();
    }


    private static KeyValue outcomeOf(StandardReference standardReference) {
        return Optional.ofNullable(standardReference)
            .map(standardReference1 -> KeyValue.builder()
                                        .code(standardReference.getCodeValue())
                                        .description(standardReference.getCodeDescription())
                                        .build())
            .orElse(null);
    }

    private static List<CourtReport> courtReportsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport> courtReports) {
        return courtReports.stream()
            .filter(report -> !report.isSoftDeleted())
            .map(CourtReportTransformer::courtReportOf)
            .collect(toList());
    }

}

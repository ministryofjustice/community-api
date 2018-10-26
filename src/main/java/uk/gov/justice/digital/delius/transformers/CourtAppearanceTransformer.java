package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Component
public class CourtAppearanceTransformer {
    private final CourtReportTransformer courtReportTransformer;
    private final CourtTransformer courtTransformer;

    public CourtAppearanceTransformer(CourtReportTransformer courtReportTransformer, CourtTransformer courtTransformer) {
        this.courtReportTransformer = courtReportTransformer;
        this.courtTransformer = courtTransformer;
    }

    public CourtAppearance courtAppearanceOf(uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance) {
        return CourtAppearance.builder()
            .courtAppearanceId(courtAppearance.getCourtAppearanceId())
            .appearanceDate(courtAppearance.getAppearanceDate())
            .crownCourtCalendarNumber(courtAppearance.getCrownCourtCalendarNumber())
            .bailConditions(courtAppearance.getBailConditions())
            .courtNotes(courtAppearance.getCourtNotes())
            .eventId(courtAppearance.getEventId())
            .teamId(courtAppearance.getTeamId())
            .staffId(courtAppearance.getStaffId())
            .court(courtTransformer.courtOf(courtAppearance.getCourt()))
            .appearanceTypeId(courtAppearance.getAppearanceTypeId())
            .pleaId(courtAppearance.getPleaId())
            .outcome(outcomeOf(courtAppearance.getOutcome()))
            .remandStatusId(courtAppearance.getRemandStatusId())
            .createdDatetime(courtAppearance.getCreatedDatetime())
            .lastUpdatedDatetime(courtAppearance.getLastUpdatedDatetime())
            .offenderId(courtAppearance.getOffenderId())
            .courtReports(courtReportsOf(courtAppearance.getCourtReports()))
            .build();
    }

    private KeyValue outcomeOf(StandardReference standardReference) {
        return Optional.ofNullable(standardReference)
            .map(standardReference1 -> KeyValue.builder()
                                        .code(standardReference.getCodeValue())
                                        .description(standardReference.getCodeDescription())
                                        .build())
            .orElse(null);
    }

    private List<CourtReport> courtReportsOf(List<uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport> courtReports) {
        return courtReports.stream()
            .filter(report -> !convertToBoolean(report.getSoftDeleted()))
            .map(courtReportTransformer::courtReportOf)
            .collect(toList());
    }

}

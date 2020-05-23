package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Component
public class CourtAppearanceTransformer {
    private final LookupSupplier lookupSupplier;

    public CourtAppearanceTransformer(LookupSupplier lookupSupplier) {
        this.lookupSupplier = lookupSupplier;
    }

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

    public uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearanceOf(
            Long offenderId,
            Event event,
            uk.gov.justice.digital.delius.data.api.CourtAppearance courtAppearance) {
        return uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance
                .builder()
                .appearanceDate(courtAppearance.getAppearanceDate())
                .courtReports(null) // TODO adding court reports is a future task
                .teamId(null) // TODO associating with a team is a future task depending on research
                .staffId(null) // TODO associating with a team is a future task depending on research
                .event(event)
                .offenderId(offenderId)
                .softDeleted(0L)
                .outcome(Optional.ofNullable(courtAppearance.getOutcome()).map(outcome -> lookupSupplier.courtAppearanceOutcomeSupplier().apply(outcome.getCode())).orElse(null) )
                .courtNotes(courtAppearance.getCourtNotes())
                .bailConditions(courtAppearance.getBailConditions())
                .createdByUserId(lookupSupplier.userSupplier().get().getUserId())
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedUserId(lookupSupplier.userSupplier().get().getUserId())
                .lastUpdatedDatetime(LocalDateTime.now())
                .court(lookupSupplier.courtSupplier().apply(courtAppearance.getCourt().getCourtId()))
                .appearanceTypeId(courtAppearance.getAppearanceTypeId())
                .crownCourtCalendarNumber(courtAppearance.getCrownCourtCalendarNumber())
                .partitionAreaId(0L)
                .remandStatusId(courtAppearance.getRemandStatusId())
                .pleaId(courtAppearance.getPleaId())
                .rowVersion(1L)
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
            .filter(report -> !convertToBoolean(report.getSoftDeleted()))
            .map(CourtReportTransformer::courtReportOf)
            .collect(toList());
    }

}

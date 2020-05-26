package uk.gov.justice.digital.delius.entitybuilders;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class CourtAppearanceEntityBuilder {
    private final LookupSupplier lookupSupplier;

    public CourtAppearanceEntityBuilder(LookupSupplier lookupSupplier) {
        this.lookupSupplier = lookupSupplier;
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
                .outcome(Optional.ofNullable(courtAppearance.getOutcome())
                        .map(outcome -> lookupSupplier.courtAppearanceOutcomeSupplier().apply(outcome.getCode()))
                        .orElse(null))
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


}

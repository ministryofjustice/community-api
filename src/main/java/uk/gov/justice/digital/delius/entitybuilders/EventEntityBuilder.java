package uk.gov.justice.digital.delius.entitybuilders;

import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.OrderManager;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.helpers.FluentHelper.not;
import static uk.gov.justice.digital.delius.service.LookupSupplier.INITIAL_ORDER_ALLOCATION;
import static uk.gov.justice.digital.delius.service.LookupSupplier.TRANSFER_CASE_INITIAL_REASON;

@Component
public class EventEntityBuilder {
    private final MainOffenceEntityBuilder mainOffenceEntityBuilder;
    private final AdditionalOffenceEntityBuilder additionalOffenceEntityBuilder;
    private final CourtAppearanceEntityBuilder courtAppearanceEntityBuilder;
    private final LookupSupplier lookupSupplier;

    public EventEntityBuilder(MainOffenceEntityBuilder mainOffenceEntityBuilder, AdditionalOffenceEntityBuilder additionalOffenceEntityBuilder, CourtAppearanceEntityBuilder courtAppearanceEntityBuilder, LookupSupplier lookupSupplier) {
        this.mainOffenceEntityBuilder = mainOffenceEntityBuilder;
        this.additionalOffenceEntityBuilder = additionalOffenceEntityBuilder;
        this.courtAppearanceEntityBuilder = courtAppearanceEntityBuilder;
        this.lookupSupplier = lookupSupplier;
    }


    public Event eventOf(
            Long offenderId,
            CourtCase courtCase,
            String eventNumber) {
        val event = Event
                .builder()
                .offenderId(offenderId)
                .activeFlag(true)
                .referralDate(courtCase.getReferralDate())
                .disposal(null) // TODO sentencing is done in a later phase
                .partitionAreaId(0L)
                .rowVersion(1L)
                .softDeleted(false)
                .convictionDate(courtCase.getConvictionDate())
                .inBreach(false)
                .eventNumber(eventNumber)
                .createdByUserId(lookupSupplier.userSupplier().get().getUserId())
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedUserId(lookupSupplier.userSupplier().get().getUserId())
                .lastUpdatedDatetime(LocalDateTime.now())
                .pendingTransfer(0L)
                .postSentenceSupervisionRequirementFlag(0L)
                .build();

        event.setMainOffence(mainOffenceEntityBuilder.mainOffenceOf(offenderId, mainOffence(courtCase.getOffences()), event));
        event.setAdditionalOffences(additionalOffenceEntityBuilder.additionalOffencesOf(additionalOffences(courtCase.getOffences()), event));
        event.setCourtAppearances(courtAppearances(offenderId, event, courtCase.getCourtAppearance(), courtCase.getNextAppearance()));
        event.setOrderManagers(List.of(orderManager(courtCase.getOrderManager(), event)));

        return event;

    }

    private OrderManager orderManager(
            uk.gov.justice.digital.delius.data.api.OrderManager orderManager,
            Event event) {
        return OrderManager
                .builder()
                .event(event)
                .activeFlag(1L)
                .allocationDate(LocalDateTime.now())
                .allocationReason(lookupSupplier.orderAllocationReasonSupplier().apply(INITIAL_ORDER_ALLOCATION))
                .createdByUserId(lookupSupplier.userSupplier().get().getUserId())
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedUserId(lookupSupplier.userSupplier().get().getUserId())
                .lastUpdatedDatetime(LocalDateTime.now())
                .probationArea(lookupSupplier.probationAreaSupplier().apply(orderManager))
                .team(lookupSupplier.teamSupplier().apply(orderManager))
                .providerEmployee(null)
                .endDate(null)
                .orderTransferId(null)
                .partitionAreaId(0L)
                .providerTeam(null)
                .rowVersion(1L)
                .softDeleted(0L)
                .staff(lookupSupplier.staffSupplier().apply(orderManager))
                .transferReason(lookupSupplier.transferReasonSupplier().apply(TRANSFER_CASE_INITIAL_REASON))
                .build();
    }

    private static List<Offence> additionalOffences(List<Offence> offences) {
        return offences.stream().filter(not(Offence::getMainOffence)).collect(Collectors.toList());
    }
    private static Offence mainOffence(List<Offence> offences) {
        return offences.stream().filter(Offence::getMainOffence).findFirst().orElseThrow(() -> new RuntimeException("No main offence found"));
    }
    private List<CourtAppearance> courtAppearances(
            Long offenderId,
            Event event,
            uk.gov.justice.digital.delius.data.api.CourtAppearance first,
            uk.gov.justice.digital.delius.data.api.CourtAppearance next) {
        val builder = new ArrayList<CourtAppearance>();
        builder.add(courtAppearanceEntityBuilder.courtAppearanceOf(offenderId, event, first));
        return Optional.ofNullable(next).map(courtAppearance -> {
            builder.add(courtAppearanceEntityBuilder.courtAppearanceOf(offenderId, event, courtAppearance));
            return builder;
        }).orElse(new ArrayList<>());
    }

}

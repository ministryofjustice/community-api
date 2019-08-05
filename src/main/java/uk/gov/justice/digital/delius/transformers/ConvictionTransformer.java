package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.OrderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.helpers.FluentHelper.not;
import static uk.gov.justice.digital.delius.service.LookupSupplier.INITIAL_ORDER_ALLOCATION;
import static uk.gov.justice.digital.delius.service.LookupSupplier.TRANSFER_CASE_INITIAL_REASON;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class ConvictionTransformer {
    private final MainOffenceTransformer mainOffenceTransformer;
    private final AdditionalOffenceTransformer additionalOffenceTransformer;
    private final CourtAppearanceTransformer courtAppearanceTransformer;
    private final LookupSupplier lookupSupplier;

    public ConvictionTransformer(MainOffenceTransformer mainOffenceTransformer, AdditionalOffenceTransformer additionalOffenceTransformer, CourtAppearanceTransformer courtAppearanceTransformer, LookupSupplier lookupSupplier) {
        this.mainOffenceTransformer = mainOffenceTransformer;
        this.additionalOffenceTransformer = additionalOffenceTransformer;
        this.courtAppearanceTransformer = courtAppearanceTransformer;
        this.lookupSupplier = lookupSupplier;
    }

    public Conviction convictionOf(Event event) {
        return Conviction.builder()
                .active(zeroOneToBoolean(event.getActiveFlag()))
                .convictionDate(event.getConvictionDate())
                .referralDate(event.getReferralDate())
                .convictionId(event.getEventId())
                .index(event.getEventNumber())
                .offences(offencesOf(event))
                .sentence(Optional.ofNullable(event.getDisposal()).map(this::sentenceOf).orElse(null))
                .inBreach(zeroOneToBoolean(event.getInBreach()))
                .latestCourtAppearanceOutcome(Optional.ofNullable(event.getCourtAppearances()).map(this::outcomeOf).orElse(null))
                .build();
    }

    private KeyValue outcomeOf(List<CourtAppearance> courtAppearances) {
        return courtAppearances
                .stream()
                .filter(courtAppearance -> courtAppearance.getOutcome() != null).max(Comparator.comparing(CourtAppearance::getAppearanceDate))
                .map(courtAppearance -> outcomeOf(courtAppearance.getOutcome()))
                .orElse(null);
    }
    private KeyValue outcomeOf(StandardReference outcome) {
        return KeyValue
                .builder()
                .description(outcome.getCodeDescription())
                .code(outcome.getCodeValue())
                .build();
    }


    private List<Offence> offencesOf(Event event) {
        return ImmutableList.<Offence>builder()
                .addAll(Optional.ofNullable(event.getMainOffence()).map(mainOffence -> ImmutableList.of(mainOffenceTransformer.offenceOf(mainOffence))).orElse(ImmutableList.of()) )
                .addAll(additionalOffenceTransformer.offencesOf(event.getAdditionalOffences()))
                .build();
    }

    private Sentence sentenceOf(Disposal disposal) {
        return Sentence.builder()
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
                .description(Optional.ofNullable(disposal.getDisposalType())
                        .map(DisposalType::getDescription)
                        .orElse(null))
                .build();
    }

    public Event eventOf(
            Long offenderId,
            CourtCase courtCase,
            String eventNumber) {
        val event = Event
                .builder()
                .offenderId(offenderId)
                .activeFlag(1L)
                .referralDate(courtCase.getReferralDate())
                .disposal(null) // TODO sentencing is done in a later phase
                .partitionAreaId(0L)
                .rowVersion(1L)
                .softDeleted(0L)
                .convictionDate(courtCase.getConvictionDate())
                .inBreach(0L) 
                .eventNumber(eventNumber)
                .createdByUserId(lookupSupplier.userSupplier().get().getUserId())
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedUserId(lookupSupplier.userSupplier().get().getUserId())
                .lastUpdatedDatetime(LocalDateTime.now())
                .pendingTransfer(0L)
                .postSentenceSupervisionRequirementFlag(0L)
                .build();

        event.setMainOffence(mainOffenceTransformer.mainOffenceOf(offenderId, mainOffence(courtCase.getOffences()), event));
        event.setAdditionalOffences(additionalOffenceTransformer.additionalOffencesOf(additionalOffences(courtCase.getOffences()), event));
        event.setCourtAppearances(courtAppearances(offenderId, event, courtCase.getCourtAppearance(), courtCase.getNextAppearance()));
        event.setOrderManagers(ImmutableList.of(orderManager(courtCase.getOrderManager(), event)));

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

    private List<Offence> additionalOffences(List<Offence> offences) {
        return offences.stream().filter(not(Offence::getMainOffence)).collect(Collectors.toList());
    }
    private Offence mainOffence(List<Offence> offences) {
        return offences.stream().filter(Offence::getMainOffence).findFirst().orElseThrow(() -> new RuntimeException("No main offence found"));
    }
    private List<CourtAppearance> courtAppearances(
            Long offenderId,
            Event event,
            uk.gov.justice.digital.delius.data.api.CourtAppearance first,
            uk.gov.justice.digital.delius.data.api.CourtAppearance next) {
        val builder = ImmutableList.<CourtAppearance>builder().add(courtAppearanceTransformer.courtAppearanceOf(offenderId, event, first));
        return Optional.ofNullable(next).map(courtAppearance -> builder.add(courtAppearanceTransformer.courtAppearanceOf(offenderId, event, courtAppearance))).orElse(builder).build();
    }

}

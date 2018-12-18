package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.data.api.Sentence;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class ConvictionTransformer {
    private final MainOffenceTransformer mainOffenceTransformer;
    private final AdditionalOffenceTransformer additionalOffenceTransformer;

    public ConvictionTransformer(MainOffenceTransformer mainOffenceTransformer, AdditionalOffenceTransformer additionalOffenceTransformer) {
        this.mainOffenceTransformer = mainOffenceTransformer;
        this.additionalOffenceTransformer = additionalOffenceTransformer;
    }

    public Conviction convictionOf(Event event) {
        return Conviction.builder()
                .active(zeroOneToBoolean(event.getActiveFlag()))
                .convictionDate(event.getConvictionDate())
                .referralDate(event.getReferralDate())
                .convictionId(event.getEventId())
                .index(indexOf(event.getEventNumber()))
                .offences(offencesOf(event))
                .sentence(Optional.ofNullable(event.getDisposal()).map(this::sentenceOf).orElse(null))
                .inBreach(zeroOneToBoolean(event.getInBreach()))
                .latestCourtAppearanceOutcome(Optional.ofNullable(event.getCourtAppearances()).map(this::outcomeOf).orElse(null))
                .build();
    }

    private Long indexOf(String eventNumber) {
        // no DB constraint so protected ourselves
        return Optional.ofNullable(eventNumber)
                .filter(StringUtils::hasText)
                .filter(this::isNumber)
                .map(text -> NumberUtils.parseNumber(text, Long.class))
                .orElse(null);
    }

    private boolean isNumber(String text) {
        return text.matches("\\d*");
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

}

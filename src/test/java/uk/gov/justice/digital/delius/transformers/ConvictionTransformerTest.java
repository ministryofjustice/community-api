package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ConvictionTransformerTest {
    private ConvictionTransformer transformer;

    @Before
    public void before() {
        transformer = new ConvictionTransformer(new MainOffenceTransformer(), new AdditionalOffenceTransformer());
    }

    @Test
    public void convictionIdMappedFromEventId() {
        assertThat(transformer.convictionOf(
                anEvent()
                    .toBuilder()
                    .eventId(99L)
                    .build()).getConvictionId()
        ).isEqualTo(99L);

    }

    @Test
    public void offencesCollatedFromMainAndAdditionalOffences() {
        assertThat(transformer.convictionOf(
                anEvent()
                        .toBuilder()
                        .eventId(99L)
                        .mainOffence(aMainOffence())
                        .additionalOffences(ImmutableList.of(anAdditionalOffence(), anAdditionalOffence()))
                        .build()).getOffences()
        ).hasSize(3);
    }

    @Test
    public void activeMappedForZeroOneActiveFlag() {
        assertThat((transformer.convictionOf(anEvent().toBuilder().activeFlag(1L).build()).getActive())).isTrue();
        assertThat((transformer.convictionOf(anEvent().toBuilder().activeFlag(0L).build()).getActive())).isFalse();

    }

    @Test
    public void inBreachMappedForZeroOneInBreach() {
        assertThat((transformer.convictionOf(anEvent().toBuilder().inBreach(1L).build()).getInBreach())).isTrue();
        assertThat((transformer.convictionOf(anEvent().toBuilder().inBreach(0L).build()).getInBreach())).isFalse();

    }

    @Test
    public void sentenceIsMappedWhenEvenHasDisposal() {
        assertThat((transformer.convictionOf(anEvent().toBuilder().disposal(null).build()).getSentence())).isNull();
        assertThat((transformer.convictionOf(anEvent().toBuilder().disposal(aDisposal()).build()).getSentence())).isNotNull();

    }

    @Test
    public void outcomeMappedFromLastCourtAppearance() {
        assertThat(transformer.convictionOf(
                anEvent()
                        .toBuilder()
                        .courtAppearances(ImmutableList.of(
                                aCourtAppearanceWithNoOutcome(LocalDateTime.now()),
                                aCourtAppearance("Final Review", "Y", LocalDateTime.now().minusDays(1)),
                                aCourtAppearance("Adjourned", "X", LocalDateTime.now().minusDays(2))
                                ))
                        .build()).getLatestCourtAppearanceOutcome())
                .isNotNull()
                .hasFieldOrPropertyWithValue("code", "Y")
                .hasFieldOrPropertyWithValue("description", "Final Review");

    }

    @Test
    public void indexMappedFromEventNumberString() {
        assertThat((transformer.convictionOf(anEvent().toBuilder().eventNumber("5").build()).getIndex())).isEqualTo(5L);
        assertThat((transformer.convictionOf(anEvent().toBuilder().eventNumber("55").build()).getIndex())).isEqualTo(55L);
        assertThat((transformer.convictionOf(anEvent().toBuilder().eventNumber(null).build()).getIndex())).isNull();
        assertThat((transformer.convictionOf(anEvent().toBuilder().eventNumber("").build()).getIndex())).isNull();
        assertThat((transformer.convictionOf(anEvent().toBuilder().eventNumber("chickens").build()).getIndex())).isNull();
    }

    private CourtAppearance aCourtAppearance(String outcomeDescription, String outcomeCode, LocalDateTime appearanceDate) {
        return CourtAppearance
                .builder()
                .appearanceDate(appearanceDate)
                .outcome(StandardReference
                        .builder()
                        .codeValue(outcomeCode)
                        .codeDescription(outcomeDescription)
                        .build())
                .build();
    }

    private CourtAppearance aCourtAppearanceWithNoOutcome(LocalDateTime appearanceDate) {
        return CourtAppearance
                .builder()
                .appearanceDate(appearanceDate)
                .outcome(null)
                .build();
    }

    private AdditionalOffence anAdditionalOffence() {
        return AdditionalOffence
                .builder()
                .offence(anOffence())
                .build();
    }

    private MainOffence aMainOffence() {
        return MainOffence
                .builder()
                .offence(anOffence())
                .build();
    }

    private Offence anOffence() {
        return Offence
                .builder()
                .ogrsOffenceCategory(StandardReference.builder().build())
                .build();
    }

    private Event anEvent() {
        return Event
                .builder()
                .additionalOffences(ImmutableList.of())
                .courtAppearances(ImmutableList.of())
                .build();
    }

    private Disposal aDisposal() {
        return Disposal.builder()
                .disposalId(1L)
                .event(anEvent())
                .offenderId(1L)
                .softDeleted(0L)
                .build();
    }


}
package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.repository.NsiRepository;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NsiServiceTest {

    private static final Long OFFENDER_ID = 123L;
    private static final Long EVENT_ID = 124L;
    private static final Event EVENT = Event.builder().softDeleted(false).build();
    public static final long NSI_ID = 12345L;

    @Mock
    private ConvictionService convictionService;

    @Mock
    private NsiRepository nsiRepository;

    @InjectMocks
    private NsiService nsiService;

    @DisplayName("All NSIs are fetched and returned, having been transformed")
    @Test
    void whenFetchNsisNoneFilterByCodeOrSoftDeleted() {

        final uk.gov.justice.digital.delius.jpa.standard.entity.Nsi nsiEntity = buildNsi(EVENT, "BRE");
        when(nsiRepository.findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID)).thenReturn(singletonList(nsiEntity));
        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.of(Conviction.builder().build()));

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE", "APCUS"));

        assertThat(nsiWrapper.orElseThrow().getNsis()).hasSize(1);
        assertThat(nsiWrapper.get().getNsis().get(0).getNsiType().getCode()).isEqualTo("BRE");

        verify(nsiRepository).findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID);
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository, convictionService);
    }

    @DisplayName("All NSIs filtered out because the code doesn't match on any of those fetched")
    @Test
    void whenFetchNsisAllFilterByCode() {

        final uk.gov.justice.digital.delius.jpa.standard.entity.Nsi nsiEntity1 = buildNsi(EVENT, "SPG");
        final uk.gov.justice.digital.delius.jpa.standard.entity.Nsi nsiEntity2 = buildNsi(EVENT, "SPX");
        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.of(Conviction.builder().build()));
        when(nsiRepository.findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID)).thenReturn(asList(nsiEntity1, nsiEntity2));

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE", "BRZ"));

        assertThat(nsiWrapper.orElseThrow().getNsis()).hasSize(0);

        verify(nsiRepository).findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID);
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository, convictionService);
    }

    @DisplayName("All NSIs filtered out because they are soft deleted, despite match on code")
    @Test
    void whenFetchNsisAllFilterBySoftDeleted() {

        final Event deletedEvent = Event.builder().softDeleted(true).build();
        final var nsiEntity = buildNsi(deletedEvent, "BRE");
        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.of(Conviction.builder().build()));
        when(nsiRepository.findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID)).thenReturn(singletonList(nsiEntity));

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE"));

        assertThat(nsiWrapper.orElseThrow().getNsis()).hasSize(0);

        verify(nsiRepository).findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID);
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository);
    }

    @DisplayName("Conviction exists, but there are no NSIs matching the code, return empty list.")
    @Test
    void whenFetchNsisRepoReturnsEmptyList() {

        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.of(Conviction.builder().build()));
        when(nsiRepository.findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID)).thenReturn(Collections.emptyList());

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE"));

        assertThat(nsiWrapper.orElseThrow().getNsis()).hasSize(0);
        verify(nsiRepository).findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID);
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository, convictionService);
    }

    @DisplayName("Conviction does not exist, or is not associated to the offender, return empty optional")
    @Test
    void whenFetchNsisConvictionDoesNotExist() {

        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.empty());

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE"));

        assertThat(nsiWrapper).isEmpty();
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository, convictionService);
    }

    @DisplayName("findByOffenderIdForActiveEvents")
    @Test
    void whenFindByOffenderIdForActiveEvents() {

        var today = now();
        final var nsiEntity = EntityHelper.aNsi().toBuilder()
                .nsiType(uk.gov.justice.digital.delius.jpa.standard.entity.NsiType.builder()
                        .code("BRE")
                        .build())
                .build();
        final var nsiEntity2 = EntityHelper.aNsi().toBuilder()
                .nsiType(uk.gov.justice.digital.delius.jpa.standard.entity.NsiType.builder()
                        .code("BRE")
                        .build())
                .referralDate(today)
                .build();

        when(nsiRepository.findByOffenderIdForActiveEvents(OFFENDER_ID)).thenReturn(asList(nsiEntity, nsiEntity2));

        final var nsiWrapper = nsiService.getNsiByCodesForOffenderActiveConvictions(OFFENDER_ID, Set.of("BRE"));

        assertThat(nsiWrapper.getNsis()).hasSize(2);

        verify(nsiRepository).findByOffenderIdForActiveEvents(OFFENDER_ID);
        verifyNoMoreInteractions(nsiRepository);
    }

    @DisplayName("findByOffenderIdForActiveEvents - Any soft deleted NSIs will be filtered out")
    @Test
    void whenFindByOffenderIdForActiveEventsIgnoreSoftDeletedNsis() {

        final var deletedNsiEntity = EntityHelper.aNsi().toBuilder()
                .nsiType(uk.gov.justice.digital.delius.jpa.standard.entity.NsiType.builder()
                        .code("BRE")
                        .build())
              .softDeleted(1L)
                .build();

        when(nsiRepository.findByOffenderIdForActiveEvents(OFFENDER_ID)).thenReturn(singletonList(deletedNsiEntity));

        final var nsiWrapper = nsiService.getNsiByCodesForOffenderActiveConvictions(OFFENDER_ID, Set.of("BRE"));

        assertThat(nsiWrapper.getNsis()).hasSize(0);

        verify(nsiRepository).findByOffenderIdForActiveEvents(OFFENDER_ID);
        verifyNoMoreInteractions(nsiRepository);
    }

    @DisplayName("When repo returns NSI return mapped NSI")
    @Test
    public void givenNsiExistsReturnIt() {
        var nsiEntity = buildNsi(EVENT, "BRE");
        when(nsiRepository.findById(NSI_ID)).thenReturn(Optional.of(nsiEntity));

        Optional<Nsi> actual = nsiService.getNsiById(NSI_ID);

        assertThat(actual).isPresent();
        assertThat(actual.get().getNsiType().getCode()).isEqualTo("BRE");
    }

    @DisplayName("When repo returns null return empty")
    @Test
    public void givenNsiDoesNotExistReturnNull() {
        when(nsiRepository.findById(NSI_ID)).thenReturn(Optional.empty());

        Optional<Nsi> actual = nsiService.getNsiById(NSI_ID);

        assertThat(actual).isEmpty();
    }

    public static uk.gov.justice.digital.delius.jpa.standard.entity.Nsi buildNsi(final Event event, final String typeCode) {
        return EntityHelper.aNsi().toBuilder()
                .nsiType(uk.gov.justice.digital.delius.jpa.standard.entity.NsiType.builder()
                    .code(typeCode)
                    .description("Some description")
                    .build())
                .event(event)
            .build();
    }

    @Nested
    class GetNonExpiredRecallNsiForOffenderActiveConvictions {

        @Test
        @DisplayName("non recalls are excluded")
        void nonRecallsAreExcluded() {
            when(nsiRepository.findByOffenderIdForActiveEvents(any())).thenReturn(List.of(buildNsi(EVENT, "BRE")));

            assertThat(nsiService.getNonExpiredRecallNsiForOffenderActiveConvictions(OFFENDER_ID).getNsis()).hasSize(0);
        }

        @Test
        @DisplayName("an recall NSI with no event would not get excluded")
        void anRecallNSIWithNoEventWouldNotGetExcluded() {
            // scenario that can't happen assuming Delius data is in a valid state - but tested to ensure it does no error
            when(nsiRepository.findByOffenderIdForActiveEvents(any())).thenReturn(List.of(buildNsi(null, "REC")));

            assertThat(nsiService.getNonExpiredRecallNsiForOffenderActiveConvictions(OFFENDER_ID).getNsis()).hasSize(1);
        }

        @Test
        @DisplayName("a recall NSI with no custodial sentence would not get excluded")
        void aRecallNSIWithNoCustodialSentenceWouldNotGetExcluded() {
            // scenario that can't happen assuming Delius data is in a valid state - but tested to ensure it does no error
            when(nsiRepository.findByOffenderIdForActiveEvents(any())).thenReturn(List.of(buildNsi(EntityHelper.anEvent(), "REC")));

            assertThat(nsiService.getNonExpiredRecallNsiForOffenderActiveConvictions(OFFENDER_ID).getNsis()).hasSize(1);
        }

        @Test
        @DisplayName("a recall NSI with a future LED will not be excluded")
        void aRecallNSIWithAFutureLEDWillNotBeExcluded() {
            when(nsiRepository.findByOffenderIdForActiveEvents(any())).thenReturn(List.of(aRecall(LocalDate.now().plusDays(1))));

            assertThat(nsiService.getNonExpiredRecallNsiForOffenderActiveConvictions(OFFENDER_ID).getNsis()).hasSize(1);
        }

        @Test
        @DisplayName("a recall with a past LED will be excluded")
        void aRecallWithAPastLEDWillBeExcluded() {
            when(nsiRepository.findByOffenderIdForActiveEvents(any())).thenReturn(List.of(aRecall(LocalDate.now().minusDays(1))));

            assertThat(nsiService.getNonExpiredRecallNsiForOffenderActiveConvictions(OFFENDER_ID).getNsis()).hasSize(0);
        }


        private uk.gov.justice.digital.delius.jpa.standard.entity.Nsi aRecall(LocalDate licenceExpiryDate) {
            return buildNsi(EntityHelper.aCustodyEvent(
                EVENT_ID,
                List.of(EntityHelper.aKeyDate("LED",
                    "Licence Expiry Date",
                    licenceExpiryDate))),
                "REC");
        }
    }

}

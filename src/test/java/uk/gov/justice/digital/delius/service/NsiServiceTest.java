package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.repository.NsiRepository;
import uk.gov.justice.digital.delius.transformers.NsiTransformer;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NsiServiceTest {

    private static final Long OFFENDER_ID = 123L;
    private static final Long EVENT_ID = 124L;
    private static final Event EVENT = Event.builder().softDeleted(0L).build();
    public static final long NSI_ID = 12345L;

    @Mock
    private ConvictionService convictionService;

    @Mock
    private NsiRepository nsiRepository;

    @Mock
    private Nsi nsi;

    @Mock
    private NsiTransformer nsiTransformer;

    @InjectMocks
    private NsiService nsiService;

    @DisplayName("All NSIs are fetched and returned, having been transformed")
    @Test
    void whenFetchNsisNoneFilterByCodeOrSoftDeleted() {

        final uk.gov.justice.digital.delius.jpa.standard.entity.Nsi nsiEntity = buildNsi(EVENT, "BRE");
        when(nsiRepository.findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID)).thenReturn(singletonList(nsiEntity));
        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.of(Conviction.builder().build()));
        when(nsiTransformer.nsiOf(nsiEntity)).thenReturn(nsi);

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE", "APCUS"));

        assertThat(nsiWrapper.get().getNsis()).hasSize(1);
        assertThat(nsiWrapper.get().getNsis()).contains(nsi);

        verify(nsiTransformer).nsiOf(nsiEntity);
        verify(nsiRepository).findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID);
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository, nsiTransformer, convictionService);
    }

    @DisplayName("All NSIs filtered out because the code doesn't match on any of those fetched")
    @Test
    void whenFetchNsisAllFilterByCode() {

        final uk.gov.justice.digital.delius.jpa.standard.entity.Nsi nsiEntity1 = buildNsi(EVENT, "SPG");
        final uk.gov.justice.digital.delius.jpa.standard.entity.Nsi nsiEntity2 = buildNsi(EVENT, "SPX");
        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.of(Conviction.builder().build()));
        when(nsiRepository.findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID)).thenReturn(asList(nsiEntity1, nsiEntity2));

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE", "BRZ"));

        assertThat(nsiWrapper.get().getNsis()).hasSize(0);

        verify(nsiRepository).findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID);
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository, nsiTransformer, convictionService);
    }

    @DisplayName("All NSIs filtered out because they are soft deleted, despite match on code")
    @Test
    void whenFetchNsisAllFilterBySoftDeleted() {

        final Event deletedEvent = Event.builder().softDeleted(1L).build();
        final var nsiEntity = buildNsi(deletedEvent, "BRE");
        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.of(Conviction.builder().build()));
        when(nsiRepository.findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID)).thenReturn(singletonList(nsiEntity));

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE"));

        assertThat(nsiWrapper.get().getNsis()).hasSize(0);

        verify(nsiRepository).findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID);
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository, nsiTransformer);
    }

    @DisplayName("Conviction exists, but there are no NSIs matching the code, return empty list.")
    @Test
    void whenFetchNsisRepoReturnsEmptyList() {

        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.of(Conviction.builder().build()));
        when(nsiRepository.findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID)).thenReturn(Collections.emptyList());

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE"));

        assertThat(nsiWrapper.get().getNsis()).hasSize(0);
        verify(nsiRepository).findByEventIdAndOffenderId(EVENT_ID, OFFENDER_ID);
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository, nsiTransformer, convictionService);
    }

    @DisplayName("Conviction does not exist, or is not associated to the offender, return empty optional")
    @Test
    void whenFetchNsisConvictionDoesNotExist() {

        when(convictionService.convictionFor(OFFENDER_ID, EVENT_ID)).thenReturn(Optional.empty());

        final var nsiWrapper = nsiService.getNsiByCodes(OFFENDER_ID, EVENT_ID, Set.of("BRE"));

        assertThat(nsiWrapper).isEmpty();
        verify(convictionService).convictionFor(OFFENDER_ID, EVENT_ID);
        verifyNoMoreInteractions(nsiRepository, nsiTransformer, convictionService);
    }

    @DisplayName("When repo returns NSI return mapped NSI")
    @Test
    public void givenNsiExistsReturnIt() {
        var nsiEntity = buildNsi(EVENT, "BRE");
        when(nsiRepository.findById(NSI_ID)).thenReturn(Optional.of(nsiEntity));
        when(nsiTransformer.nsiOf(nsiEntity)).thenReturn(nsi);

        Optional<Nsi> actual = nsiService.getNsiById(NSI_ID);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(nsi);
    }

    @DisplayName("When repo returns null return empty")
    @Test
    public void givenNsiDoesNotExistReturnNull() {
        var nsiEntity = buildNsi(EVENT, "BRE");
        when(nsiRepository.findById(NSI_ID)).thenReturn(Optional.empty());

        Optional<Nsi> actual = nsiService.getNsiById(NSI_ID);

        assertThat(actual).isEmpty();
    }

    public static uk.gov.justice.digital.delius.jpa.standard.entity.Nsi buildNsi(final Event event, final String typeCode) {
        return uk.gov.justice.digital.delius.jpa.standard.entity.Nsi.builder()
                .nsiType(uk.gov.justice.digital.delius.jpa.standard.entity.NsiType.builder()
                    .code(typeCode)
                    .description("Some description")
                    .build())
                .event(event)
            .build();
    }

}

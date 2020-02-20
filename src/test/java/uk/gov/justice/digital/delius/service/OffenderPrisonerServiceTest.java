package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderPrisoner;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderPrisonerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

class OffenderPrisonerServiceTest {
    private OffenderPrisonerRepository offenderPrisonerRepository;
    private EventRepository eventRepository;
    private OffenderPrisonerService offenderPrisonerService;

    @Captor
    private ArgumentCaptor<Set<OffenderPrisoner>> offenderPrisonersCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        offenderPrisonerRepository = mock(OffenderPrisonerRepository.class);
        eventRepository = mock(EventRepository.class);

        offenderPrisonerService = new OffenderPrisonerService(eventRepository, offenderPrisonerRepository);
        when(eventRepository.findByOffenderId(anyLong())).thenReturn(List.of(aCustodyEvent()));
    }

    @Test
    void willDeleteExistingByOffenderId() {
        offenderPrisonerService.refreshOffenderPrisonersFor(anOffender().toBuilder().offenderId(99L).build());

        verify(offenderPrisonerRepository).deleteAllByOffenderId(99L);
    }

    @Test
    void willCollateAllEventsWithPrisonerNumbers() {
        when(eventRepository.findByOffenderId(99L)).thenReturn(List.of(
                anEvent(),
                custodyEvent("12345A", LocalDate.now()),
                custodyEvent("12345B", LocalDate.now().minusDays(1)),
                custodyEvent("12345C", LocalDate.now().minusDays(2)),
                custodyEvent("12345D", LocalDate.now().minusDays(3)),
                custodyEvent(null, LocalDate.now().minusDays(4)),
                anEvent(),
                anEvent()
        ));

        offenderPrisonerService.refreshOffenderPrisonersFor(anOffender().toBuilder().offenderId(99L).build());

        verify(offenderPrisonerRepository).saveAll(offenderPrisonersCaptor.capture());

        assertThat(offenderPrisonersCaptor.getValue()).containsExactlyInAnyOrder(
                OffenderPrisoner.builder().prisonerNumber("12345A").offenderId(99L).build(),
                OffenderPrisoner.builder().prisonerNumber("12345B").offenderId(99L).build(),
                OffenderPrisoner.builder().prisonerNumber("12345C").offenderId(99L).build(),
                OffenderPrisoner.builder().prisonerNumber("12345D").offenderId(99L).build()
        );
    }

    @Test
    void willSetPrisonNumberFromLatestCustodialEvent() {
        when(eventRepository.findByOffenderId(99L)).thenReturn(List.of(
                anEvent(),
                custodyEvent("12345C", LocalDate.now().minusDays(2)),
                custodyEvent("12345B", LocalDate.now().minusDays(1)),
                custodyEvent("12345A", LocalDate.now()),
                custodyEvent("12345D", LocalDate.now().minusDays(3)),
                custodyEvent(null, LocalDate.now().minusDays(4)),
                anEvent(),
                anEvent()
        ));

        var offender = offenderPrisonerService.refreshOffenderPrisonersFor(anOffender().toBuilder().offenderId(99L).build());
        assertThat(offender.getMostRecentPrisonerNumber()).isEqualTo("12345A");

    }

    private Event custodyEvent(String prisonNumber, LocalDate disposalStartDate) {
        final var custody = aCustodyEvent()
                .getDisposal()
                .getCustody()
                .toBuilder()
                .prisonerNumber(prisonNumber)
                .build();
        final var disposal = aCustodyEvent()
                .getDisposal()
                .toBuilder()
                .custody(custody)
                .startDate(disposalStartDate)
                .build();
        return aCustodyEvent()
                .toBuilder()
                .disposal(disposal)
                .build();
    }
}
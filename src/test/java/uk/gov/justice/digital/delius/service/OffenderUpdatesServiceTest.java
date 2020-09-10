package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.digital.delius.data.api.OffenderDelta;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OffenderUpdatesServiceTest {

    private final OffenderDeltaService offenderDeltaService = mock(OffenderDeltaService.class);
    private final OffenderUpdatesService offenderUpdatesService = new OffenderUpdatesService(offenderDeltaService);

    @AfterEach
    public void resetRetries() {
        ReflectionTestUtils.setField(offenderUpdatesService, "retries", 10);
    }

    @Test
    public void retrievesOffenderDelta() {
        when(offenderDeltaService.lockNext()).thenReturn(Optional.of(anOffenderDelta()));

        final var offenderDelta = offenderUpdatesService.getNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderId()).isEqualTo(2L);
        verify(offenderDeltaService).lockNext();
    }

    @Test
    public void returnsEmptyIfNotFound() {
        when(offenderDeltaService.lockNext()).thenReturn(Optional.empty());

        final var offenderDelta = offenderUpdatesService.getNextUpdate();

        assertThat(offenderDelta.isPresent()).isFalse();
        verify(offenderDeltaService).lockNext();
    }

    @Test
    public void retrievesOffenderDeltaAfterRetry() {
        when(offenderDeltaService.lockNext())
                .thenThrow(new ConcurrencyFailureException("some lock message"))
                .thenReturn(Optional.of(anOffenderDelta()));

        final var offenderDelta = offenderUpdatesService.getNextUpdate().orElseThrow();

        assertThat(offenderDelta.getOffenderId()).isEqualTo(2L);
        verify(offenderDeltaService, times(2)).lockNext();
    }

    @Test
    public void returnsEmptyIfRetryingFails() {
        ReflectionTestUtils.setField(offenderUpdatesService, "retries", 2);
        when(offenderDeltaService.lockNext())
                .thenThrow(new ConcurrencyFailureException("some lock message"))
                .thenThrow(new ConcurrencyFailureException("some lock message"));

        final var offenderDelta = offenderUpdatesService.getNextUpdate();

        assertThat(offenderDelta.isPresent()).isFalse();
        verify(offenderDeltaService, times(2)).lockNext();
    }

    @Test
    public void returnsEmptyIfNotFoundAfterRetry() {
        ReflectionTestUtils.setField(offenderUpdatesService, "retries", 2);
        when(offenderDeltaService.lockNext())
                .thenThrow(new ConcurrencyFailureException("some lock message"))
                .thenReturn(Optional.empty());

        final var offenderDelta = offenderUpdatesService.getNextUpdate();

        assertThat(offenderDelta.isPresent()).isFalse();
        verify(offenderDeltaService, times(2)).lockNext();
    }

    private OffenderDelta anOffenderDelta() {
        return OffenderDelta.builder()
                .offenderDeltaId(1L)
                .offenderId(2L)
                .action("UPSERT")
                .status("INPROGRESS")
                .sourceTable("OFFENDER")
                .sourceRecordId(3L)
                .dateChanged(LocalDateTime.now().minusHours(1))
                .build();
    }
}

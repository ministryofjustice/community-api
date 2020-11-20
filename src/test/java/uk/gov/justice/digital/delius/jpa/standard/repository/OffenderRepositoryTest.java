package uk.gov.justice.digital.delius.jpa.standard.repository;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

class OffenderRepositoryTest {
    private final OffenderRepository offenderRepository = mock(OffenderRepository.class);


    @Nested
    class FindMostLikelyByNomsNumber {
        @BeforeEach
        void setUp() {
            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenCallRealMethod();
        }

        @Test
        @DisplayName("will return  empty when no record found")
        void willReturnARightEmptyWhenNoRecordFound() {
            when(offenderRepository.findAllByNomsNumber(any())).thenReturn(List.of());

            assertThat(offenderRepository.findMostLikelyByNomsNumber("A10101A")).isEqualTo(Either.right(Optional.empty()));
        }

        @Test
        @DisplayName("will return offender when single record found")
        void willReturnRightOffenderWhenSingleRecordFound() {
            final var expectedOffender = anOffender();
            when(offenderRepository.findAllByNomsNumber(any())).thenReturn(List.of(expectedOffender));

            assertThat(offenderRepository.findMostLikelyByNomsNumber("A10101A")).isEqualTo(Either.right(Optional.of(expectedOffender)));
        }

        @Test
        @DisplayName("will return offender when multiple records found but only one active")
        void willReturnRightOffenderWhenMultipleRecordsOneActive() {
            final var expectedOffender = anOffender().toBuilder().currentDisposal(1L).build();
            final var inactiveOffender = anOffender().toBuilder().currentDisposal(0L).build();
            when(offenderRepository.findAllByNomsNumber(any())).thenReturn(List.of(expectedOffender, inactiveOffender));

            assertThat(offenderRepository.findMostLikelyByNomsNumber("A10101A")).isEqualTo(Either.right(Optional.of(expectedOffender)));
        }

        @Test
        @DisplayName("will return error when multiple records found but all are active")
        void willReturnErrorWhenMultipleRecordsAllActive() {
            when(offenderRepository.findAllByNomsNumber(any())).thenReturn(List.of(
                    anOffender().toBuilder().currentDisposal(1L).build(),
                    anOffender().toBuilder().currentDisposal(1L).build()));

            assertThat(offenderRepository.findMostLikelyByNomsNumber("A10101A").isLeft()).isTrue();
        }

        @Test
        @DisplayName("will return error when multiple records found but all are inactive")
        void willReturnErrorWhenMultipleRecordsAllInactive() {
            when(offenderRepository.findAllByNomsNumber(any())).thenReturn(List.of(
                    anOffender().toBuilder().currentDisposal(0L).build(),
                    anOffender().toBuilder().currentDisposal(0L).build()));

            assertThat(offenderRepository.findMostLikelyByNomsNumber("A10101A").isLeft()).isTrue();
        }
    }
}
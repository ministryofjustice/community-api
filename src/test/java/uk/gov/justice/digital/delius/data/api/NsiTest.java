package uk.gov.justice.digital.delius.data.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.digital.delius.data.api.Nsi.OutcomeType;
import uk.gov.justice.digital.delius.data.api.Nsi.Status;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NsiTest {
    @Nested
    class IsOutcomeRecall {
        @SuppressWarnings("unused")
        private static Stream<Arguments> outcomeMap() {
            return Stream.of(
                Arguments.of(OutcomeType.REC01, true),
                Arguments.of(OutcomeType.REC02, true),
                Arguments.of(OutcomeType.REC03, false),
                Arguments.of(OutcomeType.REC04, false),
                Arguments.of(OutcomeType.REC05, false)
            );
        }

        @ParameterizedTest
        @DisplayName("outcome is recalled")
        @MethodSource("outcomeMap")
        void outcomeIsRecalled(OutcomeType outcomeTypeCode, boolean isRecall) {
            final var nsi = Nsi.builder().nsiOutcome(KeyValue.builder().code(outcomeTypeCode.name()).build()).build();
            assertThat(nsi.isOutcomeRecall()).isEqualTo(isRecall);
        }

        @Test
        @DisplayName("outcome is null when no outcome")
        void outcomeIsNullWhenNoOutcome() {
            final var nsi = Nsi.builder().nsiOutcome(null).build();
            assertThat(nsi.isOutcomeRecall()).isNull();
        }

        @Test
        @DisplayName("outcome is null when code is not recognisable recall outcome")
        void outcomeIsNotRecallWhenCodeIsNotRecognisable() {
            final var nsi = Nsi.builder().nsiOutcome(KeyValue.builder().code("BANANA").build()).build();
            assertThat(nsi.isOutcomeRecall()).isNull();
        }
    }

    @Nested
    class IsRecallRejectedOrWithdrawn {
        @SuppressWarnings("unused")
        private static Stream<Arguments> outcomeMap() {
            return Stream.of(
                Arguments.of(OutcomeType.REC01, false),
                Arguments.of(OutcomeType.REC02, false),
                Arguments.of(OutcomeType.REC03, true),
                Arguments.of(OutcomeType.REC04, true),
                Arguments.of(OutcomeType.REC05, true)
            );
        }

        @SuppressWarnings("unused")
        private static Stream<Arguments> statusMap() {
            return Stream.of(
                Arguments.of(Status.REC01, false),
                Arguments.of(Status.REC02, false),
                Arguments.of(Status.REC03, false),
                Arguments.of(Status.REC04, false),
                Arguments.of(Status.REC05, true),
                Arguments.of(Status.REC06, false),
                Arguments.of(Status.REC07, false),
                Arguments.of(Status.REC08, false),
                Arguments.of(Status.REC09, false),
                Arguments.of(Status.REC10, true)
            );
        }

        @ParameterizedTest
        @DisplayName("is rejected or withdrawn with no outcome is recalled")
        @MethodSource("statusMap")
        void isRejectedOrWithdrawnWithNoOutcome(Status status, boolean isRejectedOrWithdrawn) {
            final var nsi = Nsi
                .builder()
                .nsiOutcome(null)
                .nsiStatus(KeyValue.builder().code(status.name()).build())
                .build();
            assertThat(nsi.isRecallRejectedOrWithdrawn()).isEqualTo(isRejectedOrWithdrawn);
        }

        @ParameterizedTest
        @DisplayName("is rejected or withdrawn with outcome present where status is not rejected")
        @MethodSource("outcomeMap")
        void isRejectedOrWithdrawnWithOutcomePresent(OutcomeType outcomeTypeCode, boolean isRejectedOrWithdrawn) {
            assertThat(Status.REC01.getIsRejectedOrWithdrawn()).isFalse();

            final var nsi = Nsi
                .builder()
                .nsiStatus(KeyValue.builder().code(Status.REC01.name()).build())
                .nsiOutcome(KeyValue.builder().code(outcomeTypeCode.name()).build())
                .build();
            assertThat(nsi.isRecallRejectedOrWithdrawn()).isEqualTo(isRejectedOrWithdrawn);
        }

        @Test
        @DisplayName("is rejected or withdrawn is null when status is not a recall status")
        void isRejectedOrWithdrawnIsNullWhenStatusIsNotARecallStatus() {
            final var nsi = Nsi.builder().nsiStatus(KeyValue.builder().code("BANANA").build()).build();
            assertThat(nsi.isRecallRejectedOrWithdrawn()).isNull();
        }
    }
}

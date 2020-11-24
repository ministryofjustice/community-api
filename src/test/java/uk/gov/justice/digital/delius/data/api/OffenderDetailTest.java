package uk.gov.justice.digital.delius.data.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OffenderDetailTest {
    @DisplayName("isActiveProbationManagedSentence")
    @Nested
    class ActiveSentence {
        @Test
        @DisplayName("is true when a current disposal is 1")
        void isTrueWhenACurrentDisposalIs1() {
            assertThat(OffenderDetail
                    .builder()
                    .currentDisposal("1")
                    .build()
                    .isActiveProbationManagedSentence()).isTrue();
        }
        @Test
        @DisplayName("is false when a current disposal is 0")
        void isFalseWhenACurrentDisposalIs0() {
            assertThat(OffenderDetail
                    .builder()
                    .currentDisposal("0")
                    .build()
                    .isActiveProbationManagedSentence()).isFalse();
        }
        @Test
        @DisplayName("is false when a current disposal is blank")
        void isFalseWhenACurrentDisposalIsBlank() {
            assertThat(OffenderDetail
                    .builder()
                    .currentDisposal(null)
                    .build()
                    .isActiveProbationManagedSentence()).isFalse();
        }
    }
}
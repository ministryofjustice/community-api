package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationTest {

    @Nested
    class GetLatestDeregistration {
        @Test
        @DisplayName("will return null when empty")
        void returnsNullWhenEmpty() {
            final var registration = Registration.builder().deregistrations(List.of()).build();
            assertThat(registration.getLatestDeregistration()).isNull();
        }

        @Test
        @DisplayName("will return the one with the latest de-registration date")
        void returnsLatest() {
            final var registration = Registration
                    .builder()
                    .deregistrations(List.of(
                            Deregistration
                                    .builder()
                                    .deregistrationId(1L)
                                    .deregistrationDate(LocalDate.now().minusYears(1))
                                    .build(),
                            Deregistration
                                    .builder()
                                    .deregistrationId(2L)
                                    .deregistrationDate(LocalDate.now().minusDays(1))
                                    .build(),
                            Deregistration
                                    .builder()
                                    .deregistrationId(3L)
                                    .deregistrationDate(LocalDate.now().minusMonths(1))
                                    .build()))
                    .build();
            assertThat(registration.getLatestDeregistration())
                    .extracting(Deregistration::getDeregistrationId)
                    .isEqualTo(2L);
        }
        @Test
        @DisplayName("will return the one with last creation date when same date")
        void returnsAnyOfLatest() {
            final var now = LocalDateTime.now();
            final var registration = Registration
                    .builder()
                    .deregistrations(List.of(
                            Deregistration
                                    .builder()
                                    .deregistrationId(1L)
                                    .deregistrationDate(LocalDate.now().minusDays(1))
                                    .createdDatetime(now.minusSeconds(60))
                                    .build(),
                            Deregistration
                                    .builder()
                                    .deregistrationId(2L)
                                    .deregistrationDate(LocalDate.now().minusDays(1))
                                    .createdDatetime(now.minusSeconds(1))
                                    .build(),
                            Deregistration
                                    .builder()
                                    .deregistrationId(3L)
                                    .deregistrationDate(LocalDate.now().minusDays(1))
                                    .createdDatetime(now.minusSeconds(20))
                                    .build()))
                    .build();
            assertThat(registration.getLatestDeregistration())
                    .extracting(Deregistration::getDeregistrationId)
                    .isEqualTo(2L);
        }
    }
}
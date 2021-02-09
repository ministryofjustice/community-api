package uk.gov.justice.digital.delius.service;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderPrimaryIdentifiersRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
class OffenderServiceTest {
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private OffenderPrimaryIdentifiersRepository offenderPrimaryIdentifiersRepository;
    @Mock
    private ConvictionService convictionService;

    private OffenderService service;

    @BeforeEach
    void setUp() {
        service = new OffenderService(offenderRepository, offenderPrimaryIdentifiersRepository, convictionService, null);
    }

    @Nested
    @DisplayName("mostLikelyOffenderIdOfNomsNumber")
    class MostLikelyOffenderIdOfNomsNumber {
        @Test
        @DisplayName("will return offender id of the most likely offender")
        void willReturnOffenderIdOfTheMostLikelyOffender() {
            when(offenderRepository.findMostLikelyByNomsNumber(any()))
                    .thenReturn(Either.right(Optional.of(anOffender().toBuilder().offenderId(99L)
                            .build())));

            assertThat(service.mostLikelyOffenderIdOfNomsNumber("A1234ZZ").get()).hasValue(99L);
        }

        @Test
        @DisplayName("will return empty if no offender found")
        void willReturnEmptyWhenNoFoundFund() {
            when(offenderRepository.findMostLikelyByNomsNumber(any()))
                    .thenReturn(Either.right(Optional.empty()));

            assertThat(service.mostLikelyOffenderIdOfNomsNumber("A1234ZZ").get()).isEmpty();
        }

        @Test
        @DisplayName("will return error if duplicates found")
        void willReturnAnErrorForDuplicates() {
            when(offenderRepository.findMostLikelyByNomsNumber(any()))
                    .thenReturn(Either.left(new OffenderRepository.DuplicateOffenderException("two found!")));

            assertThat(service.mostLikelyOffenderIdOfNomsNumber("A1234ZZ").isLeft()).isTrue();
        }

    }
}
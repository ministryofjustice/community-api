package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCourt;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {
    @Mock
    private CourtRepository courtRepository;

    private CourtService courtService;

    @BeforeEach
    void setUp() {
        courtService = new CourtService(courtRepository);
    }

    @Nested
    class GetCourt {
        @Test
        @DisplayName("will throw NotFound return when not found")
        void willThrowNotFound() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of());

            assertThatThrownBy(() -> courtService.getCourt("SHEFCC")).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Will return court even if it is inactive")
        void willReturnCourtEvenIfItIsInactive() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("N").build()));

            assertThat(courtService.getCourt("SHEFCC").getSelectable()).isFalse();
        }

        @Test
        @DisplayName("Will return court")
        void willReturnCourt() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("Y").build()));

            assertThat(courtService.getCourt("SHEFCC").getSelectable()).isTrue();
        }

        @Test
        @DisplayName("Will return active court if there are duplicates")
        void willReturnActiveCourtWhenThereAreDuplicates() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("Y").build(),
                aCourt("SHEFCC").toBuilder().selectable("N").build()));

            assertThat(courtService.getCourt("SHEFCC").getSelectable()).isTrue();
        }

        @Test
        @DisplayName("Will return any of the duplicates if they are all active")
        void willReturnAnyCourtWhenThereAreDuplicatesAllTheSame() {
            when(courtRepository.findByCode("SHEFCC")).thenReturn(List.of(
                aCourt("SHEFCC").toBuilder().selectable("Y").build(),
                aCourt("SHEFCC").toBuilder().selectable("Y").build()));

            assertThat(courtService.getCourt("SHEFCC").getSelectable()).isTrue();
        }
    }
}

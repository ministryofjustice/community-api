package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.MainOffenceRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class OffenceServiceTest {

    private OffenceService offenceService;

    @Mock
    private MainOffenceRepository mainOffenceRepository;
    @BeforeEach
    public void setUp() {
        offenceService = new OffenceService(mainOffenceRepository);

        Mockito.when(mainOffenceRepository.findByOffenderId(1L))
            .thenReturn(ImmutableList.of(
                MainOffence.builder()
                    .mainOffenceId(1L)
                    .event(Event
                            .builder()
                            .eventId(42L)
                            .additionalOffences(ImmutableList.of(
                                    AdditionalOffence.builder()
                                            .additionalOffenceId(101L)
                                            .softDeleted(1L)
                                            .offence(Offence.builder()
                                                    .ogrsOffenceCategory(StandardReference.builder().build())
                                                    .build())
                                            .build(),
                                    AdditionalOffence.builder()
                                            .additionalOffenceId(102L)
                                            .offence(Offence.builder()
                                                    .ogrsOffenceCategory(StandardReference.builder().build())
                                                    .build())
                                            .build()
                            ))
                            .build())
                    .offence(Offence.builder()
                        .ogrsOffenceCategory(StandardReference.builder().build())
                        .build())
                    .build(),
                MainOffence.builder()
                    .mainOffenceId(2L)
                    .softDeleted(1L)
                    .event(Event
                            .builder()
                            .eventId(43L)
                            .additionalOffences(ImmutableList.of(
                                    AdditionalOffence.builder()
                                            .additionalOffenceId(103L)
                                            .offence(Offence.builder()
                                                    .ogrsOffenceCategory(StandardReference.builder().build())
                                                    .build())
                                            .build()
                            ))
                            .build())
                    .offence(Offence.builder()
                        .ogrsOffenceCategory(StandardReference.builder().build())
                        .build())
                    .build(),
                MainOffence.builder()
                    .mainOffenceId(3L)
                    .event(Event
                            .builder()
                            .eventId(44L)
                            .additionalOffences(ImmutableList.of(
                                    AdditionalOffence.builder()
                                            .additionalOffenceId(104L)
                                            .offence(Offence.builder()
                                                    .ogrsOffenceCategory(StandardReference.builder().build())
                                                    .build())
                                            .build()
                            ))
                            .build())
                    .offence(Offence.builder()
                        .ogrsOffenceCategory(StandardReference.builder().build())
                        .build())
                    .build()
                )
            );


    }

    @Test
    public void softDeletedRecordsAreExcluded() {

        List<uk.gov.justice.digital.delius.data.api.Offence> offences = offenceService.offencesFor(1L);

        assertThat(offences).extracting("offenceId")
            .containsOnly("M1", "A102", "M3", "A104");
    }

}
package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.MainOffenceRepository;
import uk.gov.justice.digital.delius.transformers.AdditionalOffenceTransformer;
import uk.gov.justice.digital.delius.transformers.MainOffenceTransformer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import({OffenceService.class, MainOffenceTransformer.class, AdditionalOffenceTransformer.class})
public class OffenceServiceTest {

    @Autowired
    private OffenceService offenceService;

    @MockBean
    private MainOffenceRepository mainOffenceRepository;

    @MockBean
    private LookupSupplier lookupSupplier;

    @Before
    public void setUp() {

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
package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.transformers.AdditionalOffenceTransformer;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;
import uk.gov.justice.digital.delius.transformers.MainOffenceTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import({ConvictionService.class, ConvictionTransformer.class, MainOffenceTransformer.class, AdditionalOffenceTransformer.class})
public class ConvictionServiceTest {

    @Autowired
    private ConvictionService convictionService;

    @MockBean
    private EventRepository convictionRepository;


    @Test
    public void convictionsOrderedByCreationDate() {
        Mockito.when(convictionRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aEvent().toBuilder().eventId(99L).referralDate(LocalDate.now().minusDays(1)).build(),
                        aEvent().toBuilder().eventId(9L).referralDate(LocalDate.now().minusDays(2)).build(),
                        aEvent().toBuilder().eventId(999L).referralDate(LocalDate.now()).build()
                ));

        assertThat(convictionService.convictionsFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.Conviction::getConvictionId)
                .collect(Collectors.toList()))
                .containsSequence(999L, 99L, 9L);

    }

    @Test
    public void deletedRecordsIgnored() {
        Mockito.when(convictionRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aEvent().toBuilder().eventId(1L).build(),
                        aEvent().toBuilder().eventId(2L).softDeleted(1L).build(),
                        aEvent().toBuilder().eventId(3L).build()
                ));

        assertThat(convictionService.convictionsFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.Conviction::getConvictionId)
                .collect(Collectors.toList()))
                .contains(1L, 3L);


    }

    private Event aEvent() {
        return Event.builder()
                .referralDate(LocalDate.now())
                .additionalOffences(ImmutableList.of())
                .build();
    }

}
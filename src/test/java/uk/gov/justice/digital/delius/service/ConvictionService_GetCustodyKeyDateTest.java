package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.service.ConvictionService.SingleActiveCustodyConvictionNotFoundException;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;
import uk.gov.justice.digital.delius.transformers.CustodyKeyDateTransformer;
import uk.gov.justice.digital.delius.transformers.EventTransformer;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@RunWith(MockitoJUnitRunner.class)
public class ConvictionService_GetCustodyKeyDateTest {

    private ConvictionService convictionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private EventTransformer eventTransformer;

    @Mock
    private SpgNotificationService spgNotificationService;

    @Mock
    private LookupSupplier lookupSupplier;

    @Mock
    private IAPSNotificationService iapsNotificationService;

    @Mock
    private ContactService contactService;

    @Before
    public void setUp() {
        convictionService = new ConvictionService(true, eventRepository, offenderRepository, eventTransformer, spgNotificationService, lookupSupplier, new CustodyKeyDateTransformer(lookupSupplier), iapsNotificationService, contactService);
        when(eventRepository.findByOffenderId(anyLong())).thenReturn(ImmutableList.of(aCustodyEvent()));
    }


    @Test
    public void canGetAKeyDateByOffenderIdWhenPresent() throws SingleActiveCustodyConvictionNotFoundException {
        val today = LocalDate.now();
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", today));

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(event));

        val custodyKeyDate = convictionService.getCustodyKeyDateByOffenderId(999L, "POM1");

        assertThat(custodyKeyDate).get().isEqualTo(CustodyKeyDate
                .builder()
                .date(today)
                .type(KeyValue
                        .builder()
                        .code("POM1")
                        .description("POM Handover expected start date")
                        .build())
                .build());

    }

    @Test
    public void canGetAllKeyDateByOffenderIdWhenPresent() throws SingleActiveCustodyConvictionNotFoundException {
        val today = LocalDate.now();
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", today));
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM2", "RO responsibility handover from POM to OM expected date", today));

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(event));

        val custodyKeyDates = convictionService.getCustodyKeyDatesByOffenderId(999L);

        assertThat(custodyKeyDates).containsExactlyInAnyOrder(
                CustodyKeyDate
                        .builder()
                        .date(today)
                        .type(KeyValue
                                .builder()
                                .code("POM1")
                                .description("POM Handover expected start date")
                                .build())
                        .build(),
                CustodyKeyDate
                        .builder()
                        .date(today)
                        .type(KeyValue
                                .builder()
                                .code("POM2")
                                .description("RO responsibility handover from POM to OM expected date")
                                .build())
                        .build()
        );

    }

    @Test
    public void canGetAKeyDateByConvictionIdWhenPresent() {
        val today = LocalDate.now();
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", today));

        when(eventRepository.getOne(999L)).thenReturn(event);

        val custodyKeyDate = convictionService.getCustodyKeyDateByConvictionId(999L, "POM1");

        assertThat(custodyKeyDate).get().isEqualTo(CustodyKeyDate
                .builder()
                .date(today)
                .type(KeyValue
                        .builder()
                        .code("POM1")
                        .description("POM Handover expected start date")
                        .build())
                .build());

    }

    @Test
    public void canGetAllKeyDatesByConvictionIdWhenPresent() {
        val today = LocalDate.now();
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", today));
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM2", "RO responsibility handover from POM to OM expected date", today));

        when(eventRepository.getOne(999L)).thenReturn(event);

        val custodyKeyDates = convictionService.getCustodyKeyDatesByConvictionId(999L);

        assertThat(custodyKeyDates).containsExactlyInAnyOrder(
                CustodyKeyDate
                        .builder()
                        .date(today)
                        .type(KeyValue
                                .builder()
                                .code("POM1")
                                .description("POM Handover expected start date")
                                .build())
                        .build(),
                CustodyKeyDate
                        .builder()
                        .date(today)
                        .type(KeyValue
                                .builder()
                                .code("POM2")
                                .description("RO responsibility handover from POM to OM expected date")
                                .build())
                        .build()
        );
    }

    @Test
    public void willReturnEmptyIfKeyDateByOffenderIdKeyDateNotPresent() throws SingleActiveCustodyConvictionNotFoundException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().clear();

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(event));

        val custodyKeyDate = convictionService.getCustodyKeyDateByOffenderId(999L, "POM1");

        assertThat(custodyKeyDate).isNotPresent();

    }

    @Test
    public void willReturnEmptyListKeyDateByOffenderIdKeyDatesNotPresent() throws SingleActiveCustodyConvictionNotFoundException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().clear();

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(event));

        val custodyKeyDates = convictionService.getCustodyKeyDatesByOffenderId(999L);

        assertThat(custodyKeyDates).isEmpty();

    }

    @Test
    public void willReturnEmptyIfKeyDateByConvictionIdNotPresent() {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().clear();

        when(eventRepository.getOne(999L)).thenReturn(event);

        val custodyKeyDate = convictionService.getCustodyKeyDateByConvictionId(999L, "POM1");

        assertThat(custodyKeyDate).isNotPresent();

    }


    @Test
    public void willReturnEmptyListIfKeyDatesByConvictionIdNotPresent() {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().clear();

        when(eventRepository.getOne(999L)).thenReturn(event);

        val custodyKeyDates = convictionService.getCustodyKeyDatesByConvictionId(999L);

        assertThat(custodyKeyDates).isEmpty();
    }


    @Test
    public void shouldNotAllowKeyDateToBeRetrievedWhenMoreThanOneActiveCustodialEvent() {
        val activeCustodyEvent1 = aCustodyEvent(1L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();
        val activeCustodyEvent2 = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(activeCustodyEvent1, activeCustodyEvent2));

        assertThatThrownBy(() -> convictionService.getCustodyKeyDateByOffenderId(999L, "POM1"))
                .isInstanceOf(SingleActiveCustodyConvictionNotFoundException.class);

    }

    @Test
    public void shouldNotAllowKeyDatesToBeRetrievedWhenMoreThanOneActiveCustodialEvent() {
        val activeCustodyEvent1 = aCustodyEvent(1L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();
        val activeCustodyEvent2 = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(activeCustodyEvent1, activeCustodyEvent2));

        assertThatThrownBy(() -> convictionService.getCustodyKeyDatesByOffenderId(999L))
                .isInstanceOf(SingleActiveCustodyConvictionNotFoundException.class);

    }

    @Test
    public void shouldNotAllowKeyDateToBeRetrievedWhenNoActiveCustodialEvent() {
        val activeNotSentencedEvent = anEvent(3L)
                .toBuilder()
                .activeFlag(1L)
                .disposal(null)
                .build();
        val activeCommunitySentencedEvent = anEvent(4L).toBuilder().activeFlag(1L).disposal(aCommunityDisposal(4L)).build();

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(activeNotSentencedEvent, activeCommunitySentencedEvent));

        assertThatThrownBy(() -> convictionService.getCustodyKeyDateByOffenderId(999L, "POM1"))
                .isInstanceOf(SingleActiveCustodyConvictionNotFoundException.class);

    }

    @Test
    public void shouldNotAllowKeyDatesToBeRetrievedWhenNoActiveCustodialEvent() {
        val activeNotSentencedEvent = anEvent(3L)
                .toBuilder()
                .activeFlag(1L)
                .disposal(null)
                .build();
        val activeCommunitySentencedEvent = anEvent(4L).toBuilder().activeFlag(1L).disposal(aCommunityDisposal(4L)).build();

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(activeNotSentencedEvent, activeCommunitySentencedEvent));

        assertThatThrownBy(() -> convictionService.getCustodyKeyDatesByOffenderId(999L))
                .isInstanceOf(SingleActiveCustodyConvictionNotFoundException.class);

    }

    @Test
    public void keyDateRetrievedFromToActiveCustodyRecord() throws SingleActiveCustodyConvictionNotFoundException {
        val today = LocalDate.now();
        val activeCustodyEvent = aCustodyEvent(1L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();
        activeCustodyEvent.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", today));

        val inactiveCustodyEvent = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(0L)
                .build();
        val event = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(0L)
                .build();
        val terminatedDisposal = event.getDisposal().toBuilder().terminationDate(LocalDate.now()).build();
        val activeEventButTerminatedCustodyEvent = event.toBuilder().disposal(terminatedDisposal).build();
        val activeNotSentencedEvent = anEvent(3L)
                .toBuilder()
                .activeFlag(1L)
                .disposal(null)
                .build();
        val activeCommunitySentencedEvent = anEvent(4L).toBuilder().activeFlag(1L).disposal(aCommunityDisposal(4L)).build();

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(activeCustodyEvent, inactiveCustodyEvent, activeNotSentencedEvent, activeCommunitySentencedEvent, activeEventButTerminatedCustodyEvent));

        val custodyKeyDate = convictionService.getCustodyKeyDateByOffenderId(999L, "POM1");

        assertThat(custodyKeyDate).get().isEqualTo(CustodyKeyDate
                .builder()
                .date(today)
                .type(KeyValue
                        .builder()
                        .code("POM1")
                        .description("POM Handover expected start date")
                        .build())
                .build());

    }
    @Test
    public void keyDatesRetrievedFromToActiveCustodyRecord() throws SingleActiveCustodyConvictionNotFoundException {
        val today = LocalDate.now();
        val activeCustodyEvent = aCustodyEvent(1L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();
        activeCustodyEvent.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", today));

        val inactiveCustodyEvent = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(0L)
                .build();
        val event = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(0L)
                .build();
        val terminatedDisposal = event.getDisposal().toBuilder().terminationDate(LocalDate.now()).build();
        val activeEventButTerminatedCustodyEvent = event.toBuilder().disposal(terminatedDisposal).build();
        val activeNotSentencedEvent = anEvent(3L)
                .toBuilder()
                .activeFlag(1L)
                .disposal(null)
                .build();
        val activeCommunitySentencedEvent = anEvent(4L).toBuilder().activeFlag(1L).disposal(aCommunityDisposal(4L)).build();

        when(eventRepository.findByOffenderId(999L)).thenReturn(ImmutableList.of(activeCustodyEvent, inactiveCustodyEvent, activeNotSentencedEvent, activeCommunitySentencedEvent, activeEventButTerminatedCustodyEvent));

        val custodyKeyDates = convictionService.getCustodyKeyDatesByOffenderId(999L);

        assertThat(custodyKeyDates).containsExactly(CustodyKeyDate
                .builder()
                .date(today)
                .type(KeyValue
                        .builder()
                        .code("POM1")
                        .description("POM Handover expected start date")
                        .build())
                .build());

    }
}
package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.CreateCustodyKeyDate;
import uk.gov.justice.digital.delius.entitybuilders.EventEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.KeyDateEntityBuilder;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.service.ConvictionService.CustodyTypeCodeIsNotValidException;
import uk.gov.justice.digital.delius.service.ConvictionService.SingleActiveCustodyConvictionNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;

@RunWith(MockitoJUnitRunner.class)
public class ConvictionService_AddReplaceCustodyKeyDateTest {

    private ConvictionService convictionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private EventEntityBuilder eventEntityBuilder;

    @Mock
    private SpgNotificationService spgNotificationService;

    @Mock
    private IAPSNotificationService iapsNotificationService;

    @Mock
    private LookupSupplier lookupSupplier;

    @Mock
    private ContactService contactService;

    @Mock
    private TelemetryClient telemetryClient;

    @Captor
    private ArgumentCaptor<Event> eventArgumentCaptor;

    @Before
    public void setUp() {
        convictionService = new ConvictionService(true, eventRepository, offenderRepository, eventEntityBuilder, spgNotificationService, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(88L).build());
        when(eventRepository.findByOffenderIdWithCustody(anyLong())).thenReturn(ImmutableList.of(aCustodyEvent()));
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.of(StandardReference
                .builder()
                .codeDescription("description")
                .codeValue(code)
                .build()));
    }


    @Test
    public void addedCustodyKeyDateByOffenderIdIsSaved() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val keyDate = LocalDate.now();
        val event = aCustodyEvent(1L, new ArrayList<>());
        val keyDateType = StandardReference
                .builder()
                .codeDescription("POM Handover expected start date")
                .codeValue("POM1")
                .build();

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.of(keyDateType));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(keyDate)
                        .build());

        verify(eventRepository).saveAndFlush(eventArgumentCaptor.capture());

        assertThat(eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates()).hasSize(1);
        val custodyKeyDateToInsert = eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates().get(0);

        assertThat(custodyKeyDateToInsert.getCreatedDatetime()).isCloseTo(LocalDateTime.now(), within(500, ChronoUnit.MILLIS));
        assertThat(custodyKeyDateToInsert.getCreatedByUserId()).isEqualTo(88L);
        assertThat(custodyKeyDateToInsert.getLastUpdatedDatetime()).isCloseTo(LocalDateTime.now(), within(500, ChronoUnit.MILLIS));
        assertThat(custodyKeyDateToInsert.getLastUpdatedUserId()).isEqualTo(88L);
        assertThat(custodyKeyDateToInsert.getKeyDate()).isEqualTo(keyDate);
        assertThat(custodyKeyDateToInsert.getKeyDateType()).isEqualTo(keyDateType);
    }

    @Test
    public void spgIsNotifiedOfAddWhenAddedByOffenderId() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(spgNotificationService).notifyNewCustodyKeyDate("POM1", event);
    }

    @Test
    @DisplayName("KeyDateAdded telemetry event raised when adding a key date")
    public void telemetryEventRaisedWhenAddedByOffenderId() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        final ArgumentMatcher<Map<String, String>> standardTelemetryAttributes =
                attributes ->
                        Optional
                                .ofNullable(attributes.get("eventNumber"))
                                .filter(value -> value.equals("20"))
                                .isPresent() &&
                                Optional
                                        .ofNullable(attributes.get("offenderId"))
                                        .filter(value -> value.equals("999"))
                                        .isPresent() &&
                                Optional
                                        .ofNullable(attributes.get("eventId"))
                                        .filter(value -> value.equals("345"))
                                        .isPresent() &&
                                Optional
                                        .ofNullable(attributes.get("date"))
                                        .filter(value -> value.equals(LocalDate.now().toString()))
                                        .isPresent() &&
                                Optional
                                        .ofNullable(attributes.get("type"))
                                        .filter(value -> value.equals("POM1"))
                                        .isPresent();

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(List.of(aCustodyEvent(345L, new ArrayList<>())
                .toBuilder()
                .offenderId(999L)
                .eventNumber("20")
                .build()));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(telemetryClient).trackEvent(eq("KeyDateAdded"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void iapsIsNotNotifiedOfAddWhenAddedByOffenderIdAndWhenSentenceExpiryNotAffected() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(iapsNotificationService, never()).notifyEventUpdated(any());
    }

    @Test
    public void iapsIsNotifiedOfAddWhenAddedByOffenderIdAndWhenSentenceExpiryNotAffected() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "SED",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(iapsNotificationService).notifyEventUpdated(event);
    }


    @Test
    public void addedCustodyKeyDateByOffenderIdIsUpdatedIfPresent() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        val createdDateTime = LocalDateTime.now().minusDays(1);
        val newKeyDate = LocalDate.now();
        event.getDisposal().getCustody().getKeyDates().add(
                aKeyDate("POM1", "POM Handover expected start date", LocalDate.now().minusDays(1))
                .toBuilder()
                        .lastUpdatedDatetime(LocalDateTime.now().minusDays(1))
                        .lastUpdatedUserId(77L)
                        .createdDatetime(createdDateTime)
                        .createdByUserId(77L)
                .build()
        );

        val keyDateType = StandardReference
                .builder()
                .codeDescription("POM Handover expected start date")
                .codeValue("POM1")
                .build();

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.of(keyDateType));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(newKeyDate)
                        .build());

        verify(eventRepository).save(eventArgumentCaptor.capture());

        assertThat(eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates()).hasSize(1);
        val custodyKeyDateToInsert = eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates().get(0);

        assertThat(custodyKeyDateToInsert.getCreatedDatetime()).isEqualTo(createdDateTime);
        assertThat(custodyKeyDateToInsert.getCreatedByUserId()).isEqualTo(77L);
        assertThat(custodyKeyDateToInsert.getLastUpdatedDatetime()).isCloseTo(LocalDateTime.now(), within(500, ChronoUnit.MILLIS));
        assertThat(custodyKeyDateToInsert.getLastUpdatedUserId()).isEqualTo(88L);
        assertThat(custodyKeyDateToInsert.getKeyDate()).isEqualTo(newKeyDate);
        assertThat(custodyKeyDateToInsert.getKeyDateType()).isEqualTo(keyDateType);
    }

    @Test
    @DisplayName("KeyDateAdded telemetry event raised when adding a key date")
    public void telemetryEventRaisedWhenUpdatingByOffenderId() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        final ArgumentMatcher<Map<String, String>> standardTelemetryAttributes =
                attributes ->
                        Optional
                                .ofNullable(attributes.get("eventNumber"))
                                .filter(value -> value.equals("20"))
                                .isPresent() &&
                                Optional
                                        .ofNullable(attributes.get("offenderId"))
                                        .filter(value -> value.equals("999"))
                                        .isPresent() &&
                                Optional
                                        .ofNullable(attributes.get("eventId"))
                                        .filter(value -> value.equals("345"))
                                        .isPresent() &&
                                Optional
                                        .ofNullable(attributes.get("date"))
                                        .filter(value -> value.equals(LocalDate.now().toString()))
                                        .isPresent() &&
                                Optional
                                        .ofNullable(attributes.get("type"))
                                        .filter(value -> value.equals("POM1"))
                                        .isPresent();

        val event = aCustodyEvent(345L, new ArrayList<>())
                .toBuilder()
                .offenderId(999L)
                .eventNumber("20")
                .build();
        event.getDisposal().getCustody().getKeyDates().add(
                aKeyDate("POM1", "POM Handover expected start date", LocalDate.now().minusDays(1)));

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(telemetryClient).trackEvent(eq("KeyDateUpdated"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void spgIsNotifiedOfUpdateWhenReplacedByOffenderId() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(
                aKeyDate("POM1", "POM Handover expected start date", LocalDate.now().minusDays(1)));

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(spgNotificationService).notifyUpdateOfCustodyKeyDate("POM1", event);
    }

    @Test
    public void iapsIsNotNotifiedOfReplaceWhenAddedByOffenderIdAndWhenSentenceExpiryNotAffected() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(
                1L,
                ImmutableList.of(aKeyDate("POM1", "POM Handover expected start date", LocalDate.now().minusDays(1))));

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(iapsNotificationService, never()).notifyEventUpdated(any());
    }

    @Test
    public void iapsIsNotifiedOfUpdateWhenAddedByOffenderIdAndWhenSentenceExpiryIsAffected() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(
                1L,
                ImmutableList.of(aKeyDate("SED", "Sentence Expiry Date", LocalDate.now().minusDays(1))));

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "SED",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(iapsNotificationService).notifyEventUpdated(event);
    }

    @Test
    public void addedCustodyKeyDateByOffenderIdIsReturned() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val keyDate = LocalDate.now();

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(aCustodyEvent(1L, new ArrayList<>())));
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.of(StandardReference
                .builder()
                .codeDescription("POM Handover expected start date")
                .codeValue("POM1")
                .build()));

        val newCustodyKeyDate = convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(keyDate)
                        .build());

        assertThat(newCustodyKeyDate.getType().getCode()).isEqualTo("POM1");
        assertThat(newCustodyKeyDate.getType().getDescription()).isEqualTo("POM Handover expected start date");
        assertThat(newCustodyKeyDate.getDate()).isEqualTo(keyDate);
    }

    @Test
    public void addedCustodyKeyDateByConvictionIdIsSaved() throws CustodyTypeCodeIsNotValidException {
        val keyDate = LocalDate.now();
        val event = aCustodyEvent(1L, new ArrayList<>());
        val keyDateType = StandardReference
                .builder()
                .codeDescription("POM Handover expected start date")
                .codeValue("POM1")
                .build();

        when(eventRepository.getOne(999L)).thenReturn(event);
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.of(keyDateType));

        convictionService.addOrReplaceCustodyKeyDateByConvictionId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(keyDate)
                        .build());

        verify(eventRepository).saveAndFlush(eventArgumentCaptor.capture());

        assertThat(eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates()).hasSize(1);
        val custodyKeyDateToInsert = eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates().get(0);

        assertThat(custodyKeyDateToInsert.getCreatedDatetime()).isCloseTo(LocalDateTime.now(), within(500, ChronoUnit.MILLIS));
        assertThat(custodyKeyDateToInsert.getCreatedByUserId()).isEqualTo(88L);
        assertThat(custodyKeyDateToInsert.getLastUpdatedDatetime()).isCloseTo(LocalDateTime.now(), within(500, ChronoUnit.MILLIS));
        assertThat(custodyKeyDateToInsert.getLastUpdatedUserId()).isEqualTo(88L);
        assertThat(custodyKeyDateToInsert.getKeyDate()).isEqualTo(keyDate);
        assertThat(custodyKeyDateToInsert.getKeyDateType()).isEqualTo(keyDateType);
    }

    @Test
    public void spgIsNotifiedOfAddWhenAddedByConvictionId() throws CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        when(eventRepository.getOne(999L)).thenReturn(event);

        convictionService.addOrReplaceCustodyKeyDateByConvictionId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(spgNotificationService).notifyNewCustodyKeyDate("POM1", event);
    }

    @Test
    public void iapsIsNotNotifiedOfReplaceWhenAddedByConvictionIdAndWhenSentenceExpiryNotAffected() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(
                1L,
                ImmutableList.of(aKeyDate("POM1", "POM Handover expected start date", LocalDate.now().minusDays(1))));

        when(eventRepository.getOne(999L)).thenReturn(event);

        convictionService.addOrReplaceCustodyKeyDateByConvictionId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(iapsNotificationService, never()).notifyEventUpdated(any());
    }

    @Test
    public void iapsIsNotifiedOfUpdateWhenAddedByConvictionIdAndWhenSentenceExpiryIsAffected() throws CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(
                1L,
                ImmutableList.of(aKeyDate("SED", "Sentence Expiry Date", LocalDate.now().minusDays(1))));

        when(eventRepository.getOne(999L)).thenReturn(event);

        convictionService.addOrReplaceCustodyKeyDateByConvictionId(
                999L,
                "SED",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(iapsNotificationService).notifyEventUpdated(event);
    }


    @Test
    public void spgIsNotifiedOfUpdateWhenReplacedByConvictionId() throws CustodyTypeCodeIsNotValidException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(
                aKeyDate("POM1", "POM Handover expected start date", LocalDate.now().minusDays(1)));

        when(eventRepository.getOne(999L)).thenReturn(event);

        convictionService.addOrReplaceCustodyKeyDateByConvictionId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        verify(spgNotificationService).notifyUpdateOfCustodyKeyDate("POM1", event);
    }


    @Test
    public void addedCustodyKeyDateByConvictionIdIsReturned() throws CustodyTypeCodeIsNotValidException {
        val keyDate = LocalDate.now();
        val event = aCustodyEvent(1L, new ArrayList<>());

        when(eventRepository.getOne(999L)).thenReturn(event);
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.of(StandardReference
                .builder()
                .codeDescription("POM Handover expected start date")
                .codeValue("POM1")
                .build()));

        val newCustodyKeyDate = convictionService.addOrReplaceCustodyKeyDateByConvictionId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(keyDate)
                        .build());

        assertThat(newCustodyKeyDate.getType().getCode()).isEqualTo("POM1");
        assertThat(newCustodyKeyDate.getType().getDescription()).isEqualTo("POM Handover expected start date");
        assertThat(newCustodyKeyDate.getDate()).isEqualTo(keyDate);
    }


    @Test
    public void shouldNotAllowKeyDateToBeAddedByOffenderIdWhenInvalidKeyDateCode() {
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.empty());

        assertThatThrownBy(() -> convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build())).isInstanceOf(CustodyTypeCodeIsNotValidException.class);

    }

    @Test
    public void shouldNotAllowKeyDateToBeAddedByConvictionIdWhenInvalidKeyDateCode() {
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.empty());

        assertThatThrownBy(() -> convictionService.addOrReplaceCustodyKeyDateByConvictionId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build())).isInstanceOf(CustodyTypeCodeIsNotValidException.class);

    }

    @Test
    public void shouldNotAllowKeyDateToBeAddedWhenMoreThanOneActiveCustodialEvent() {
        val activeCustodyEvent1 = aCustodyEvent(1L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();
        val activeCustodyEvent2 = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(activeCustodyEvent1, activeCustodyEvent2));

        assertThatThrownBy(() -> convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build())).isInstanceOf(SingleActiveCustodyConvictionNotFoundException.class);

    }

    @Test
    public void shouldNotAllowKeyDateToBeAddedByOffenderIdWhenNoActiveCustodialEvent() {
        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of());

        assertThatThrownBy(() -> convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build())).isInstanceOf(SingleActiveCustodyConvictionNotFoundException.class);

    }

    @Test
    public void shouldAllowKeyDateToBeAddedByConvictionIdWhenEvenWhenCustodialEventIsNoLongerActive() throws CustodyTypeCodeIsNotValidException {
        val inactiveCustodyEvent = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(0L)
                .build();

        when(eventRepository.getOne(999L)).thenReturn(inactiveCustodyEvent);

        assertThat(convictionService.addOrReplaceCustodyKeyDateByConvictionId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build())).isNotNull();

    }

    @Test
    public void keyDateAddedToActiveCustodyRecord() throws SingleActiveCustodyConvictionNotFoundException, CustodyTypeCodeIsNotValidException {
        val activeCustodyEvent = aCustodyEvent(1L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();
        val event = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(1L)
                .build();
        val terminatedDisposal = event.getDisposal().toBuilder().terminationDate(LocalDate.now()).build();
        val activeEventButTerminatedCustodyEvent = event.toBuilder().disposal(terminatedDisposal).build();

        when(eventRepository.findByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(activeCustodyEvent, activeEventButTerminatedCustodyEvent));

        convictionService.addOrReplaceCustodyKeyDateByOffenderId(
                999L,
                "POM1",
                CreateCustodyKeyDate
                        .builder()
                        .date(LocalDate.now())
                        .build());

        assertThat(activeCustodyEvent.getDisposal().getCustody().getKeyDates()).hasSize(1);
        assertThat(activeEventButTerminatedCustodyEvent.getDisposal().getCustody().getKeyDates()).hasSize(0);

    }
}

package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.entitybuilders.EventEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.KeyDateEntityBuilder;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.service.ConvictionService.SingleActiveCustodyConvictionNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;

@ExtendWith(MockitoExtension.class)
public class ConvictionService_DeleteCustodyKeyDateTest {

    private ConvictionService convictionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private EventEntityBuilder eventEntityBuilder;

    @Mock
    private LookupSupplier lookupSupplier;

    @Captor
    private ArgumentCaptor<Event> eventArgumentCaptor;

    @Mock
    private IAPSNotificationService iapsNotificationService;

    @Mock
    private ContactService contactService;

    @Mock
    private TelemetryClient telemetryClient;


    @BeforeEach
    public void setUp() {
        final var featureSwitches = new FeatureSwitches();
        featureSwitches.getNoms().getUpdate().setKeyDates(true);
        convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient, featureSwitches);
    }


    @Test
    public void deletedCustodyKeyDateByOffenderIdIsRemoved() throws SingleActiveCustodyConvictionNotFoundException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", LocalDate.now()));
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM2", "Some other key date", LocalDate.now()));
        assertThat(event.getDisposal().getCustody().getKeyDates()).hasSize(2);

        when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.deleteCustodyKeyDateByOffenderId(
                999L,
                "POM1");

        verify(eventRepository).save(eventArgumentCaptor.capture());

        assertThat(eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates()).hasSize(1);
        assertThat(eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates().get(0).getKeyDateType().getCodeValue()).isEqualTo("POM2");
    }

    @Test
    @DisplayName("KeyDateAdded telemetry event raised when adding a key date")
    public void telemetryEventRaisedWhenUpdatingByOffenderId() throws SingleActiveCustodyConvictionNotFoundException {
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

        when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.deleteCustodyKeyDateByOffenderId(
                999L,
                "POM1");

        verify(telemetryClient).trackEvent(eq("KeyDateDeleted"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void iapsIsNotNotifiedOfDeleteByOffenderIdIdAndWhenSentenceExpiryNotAffected() throws SingleActiveCustodyConvictionNotFoundException {
        val event = aCustodyEvent(
                1L,
                listOf(aKeyDate("POM1", "POM Handover expected start date", LocalDate.now().minusDays(1))));

        when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.deleteCustodyKeyDateByOffenderId(
                999L,
                "POM1");

        verify(iapsNotificationService, never()).notifyEventUpdated(any());
    }

    @Test
    public void iapsIsNotifiedOfDeleteWhenByOffenderIdAndWhenSentenceExpiryIsAffected() throws SingleActiveCustodyConvictionNotFoundException {
        val event = aCustodyEvent(
                1L,
                listOf(aKeyDate("SED", "Sentence Expiry Date", LocalDate.now().minusDays(1))));

        when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.deleteCustodyKeyDateByOffenderId(
                999L,
                "SED");

        verify(iapsNotificationService).notifyEventUpdated(event);
    }


    @Test
    public void deletedCustodyKeyDateByConvictionIdIsRemoved() {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", LocalDate.now()));
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM2", "Some other key date", LocalDate.now()));
        assertThat(event.getDisposal().getCustody().getKeyDates()).hasSize(2);

        when(eventRepository.getOne(999L)).thenReturn(event);

        convictionService.deleteCustodyKeyDateByConvictionId(
                999L,
                "POM1");

        verify(eventRepository).save(eventArgumentCaptor.capture());

        assertThat(eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates()).hasSize(1);
        assertThat(eventArgumentCaptor.getValue().getDisposal().getCustody().getKeyDates().get(0).getKeyDateType().getCodeValue()).isEqualTo("POM2");
    }

    @Test
    public void iapsIsNotNotifiedOfDeleteByConvictionIdAndWhenSentenceExpiryNotAffected() {
        val event = aCustodyEvent(
                1L,
                listOf(aKeyDate("POM1", "POM Handover expected start date", LocalDate.now().minusDays(1))));

        when(eventRepository.getOne(999L)).thenReturn(event);

        convictionService.deleteCustodyKeyDateByConvictionId(
                999L,
                "POM1");

        verify(iapsNotificationService, never()).notifyEventUpdated(any());
    }

    @Test
    public void iapsIsNotifiedOfDeleteWhenByConvictionIdAndWhenSentenceExpiryIsAffected() {
        val event = aCustodyEvent(
                1L,
                listOf(aKeyDate("SED", "Sentence Expiry Date", LocalDate.now().minusDays(1))));

        when(eventRepository.getOne(999L)).thenReturn(event);

        convictionService.deleteCustodyKeyDateByConvictionId(
                999L,
                "SED");

        verify(iapsNotificationService).notifyEventUpdated(event);
    }


    @Test
    public void nothingIsSavedWhenCustodyKeyDateByOffenderIdIsNotFound() throws SingleActiveCustodyConvictionNotFoundException {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM2", "Some other key date", LocalDate.now()));

        when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(event));

        convictionService.deleteCustodyKeyDateByOffenderId(
                999L,
                "POM1");

        verify(eventRepository, never()).save(event);

    }

    @Test
    public void nothingIsSavedWhenCustodyKeyDateByConvictionIdIsNotFound() {
        val event = aCustodyEvent(1L, new ArrayList<>());
        event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM2", "Some other key date", LocalDate.now()));

        when(eventRepository.getOne(999L)).thenReturn(event);

        convictionService.deleteCustodyKeyDateByConvictionId(
                999L,
                "POM1");

        verify(eventRepository, never()).save(event);

    }

    @Nested
    class WithMultipleEvents {

        @Nested
        class WhenAllowMultipleEventUpdateFeatureSwitchOn {
            @BeforeEach
            void setUp() {
                final var featureSwitches = new FeatureSwitches();
                featureSwitches.getNoms().getUpdate().setCustody(true);
                featureSwitches.getNoms().getUpdate().getMultipleEvents().setUpdateKeyDates(true);
                convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient, featureSwitches);
            }

            @Test
            @DisplayName("Will delete key dates for each active custodial event")
            public void shouldAllowKeyDateToBeDeletedWhenMoreThanOneActiveCustodialEvent() {
                val activeCustodyEvent1 = aCustodyEvent(1L, List.of(aKeyDate("POM1", "POM Handover expected start date")));
                val activeCustodyEvent2 = aCustodyEvent(2L, List.of(aKeyDate("POM1", "POM Handover expected start date")));

                when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(activeCustodyEvent1, activeCustodyEvent2));

                convictionService.deleteCustodyKeyDateByOffenderId(999L, "POM1");

                verify(eventRepository).save(activeCustodyEvent1);
                verify(eventRepository).save(activeCustodyEvent2);
                verify(telemetryClient, times(2)).trackEvent(eq("KeyDateDeleted"), any(), isNull());
            }
        }

        @Nested
        class WhenAllowMultipleEventUpdateFeatureSwitchOff {
            @BeforeEach
            void setUp() {
                final var featureSwitches = new FeatureSwitches();
                featureSwitches.getNoms().getUpdate().getMultipleEvents().setUpdateKeyDates(false);
                convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient, featureSwitches);
            }

            @Test
            @DisplayName("Will not delete key dates for any active custodial event")
            public void shouldNotAllowKeyDateToBeDeletedWhenMoreThanOneActiveCustodialEvent() {
                val activeCustodyEvent1 = aCustodyEvent(1L, List.of(aKeyDate("POM1", "POM Handover expected start date")));
                val activeCustodyEvent2 = aCustodyEvent(2L, List.of(aKeyDate("POM1", "POM Handover expected start date")));

                when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(activeCustodyEvent1, activeCustodyEvent2));

                assertThatThrownBy(() -> convictionService.deleteCustodyKeyDateByOffenderId(999L, "POM1")).isInstanceOf(SingleActiveCustodyConvictionNotFoundException.class);

                verify(eventRepository, never()).save(any());
                verifyNoInteractions(telemetryClient);
            }
        }
    }


    @Test
    public void shouldAllowKeyDateToBeDeletedByConvictionIdWhenEvenWhenCustodialEventIsNoLongerActive() {
        val inactiveCustodyEvent = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(false)
                .build();
        inactiveCustodyEvent.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", LocalDate.now()));

        when(eventRepository.getOne(999L)).thenReturn(inactiveCustodyEvent);

        convictionService.deleteCustodyKeyDateByConvictionId(
                999L,
                "POM1");

        verify(eventRepository).save(any());
    }

    @Test
    public void keyDateRemovedFromActiveCustodyRecord() throws SingleActiveCustodyConvictionNotFoundException {
        val activeCustodyEvent = aCustodyEvent(1L, new ArrayList<>())
                .toBuilder()
                .activeFlag(true)
                .build();
        activeCustodyEvent.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", LocalDate.now()));

        val event = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(false)
                .build();
        val terminatedDisposal = event.getDisposal().toBuilder().terminationDate(LocalDate.now()).build();
        val activeEventButTerminatedCustodyEvent = event.toBuilder().disposal(terminatedDisposal).build();
        activeEventButTerminatedCustodyEvent.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", LocalDate.now()));

        when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(ImmutableList.of(activeCustodyEvent, activeEventButTerminatedCustodyEvent));

        convictionService.deleteCustodyKeyDateByOffenderId(
                999L,
                "POM1");

        assertThat(activeCustodyEvent.getDisposal().getCustody().getKeyDates()).hasSize(0);
        assertThat(activeEventButTerminatedCustodyEvent.getDisposal().getCustody().getKeyDates()).hasSize(1);

    }

    private static <T> List<T> listOf(T item) {
        return new ArrayList<>(singletonList(item));
    }
}

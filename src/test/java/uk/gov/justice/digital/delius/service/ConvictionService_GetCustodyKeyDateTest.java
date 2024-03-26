package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.entitybuilders.EventEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.KeyDateEntityBuilder;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.service.ConvictionService.SingleActiveCustodyConvictionNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;

@ExtendWith(MockitoExtension.class)
public class ConvictionService_GetCustodyKeyDateTest {

    private ConvictionService convictionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private EventEntityBuilder eventEntityBuilder;

    @Mock
    private LookupSupplier lookupSupplier;

    @Mock
    private IAPSNotificationService iapsNotificationService;

    @Mock
    private ContactService contactService;

    @Mock
    private TelemetryClient telemetryClient;

    @Nested class CustodyTests {
        @BeforeEach
        public void setUp() {
            final var featureSwitches = new FeatureSwitches();
            featureSwitches.getNoms().getUpdate().setKeyDates(true);
            convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient, featureSwitches);
            when(eventRepository.findActiveByOffenderIdWithCustody(anyLong())).thenReturn(List.of(aCustodyEvent()));
        }

        @Test
        public void canGetAKeyDateByOffenderIdWhenPresent() throws SingleActiveCustodyConvictionNotFoundException {
            val today = LocalDate.now();
            val event = aCustodyEvent(1L, new ArrayList<>());
            event.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", today));

            when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(List.of(event));

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
        public void willReturnEmptyIfKeyDateByOffenderIdKeyDateNotPresent() throws SingleActiveCustodyConvictionNotFoundException {
            val event = aCustodyEvent(1L, new ArrayList<>());
            event.getDisposal().getCustody().getKeyDates().clear();

            when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(List.of(event));

            val custodyKeyDate = convictionService.getCustodyKeyDateByOffenderId(999L, "POM1");

            assertThat(custodyKeyDate).isNotPresent();

        }

        @Test
        public void shouldNotAllowKeyDateToBeRetrievedWhenMoreThanOneActiveCustodialEvent() {
            val activeCustodyEvent1 = aCustodyEvent(1L, new ArrayList<>())
                .toBuilder()
                .activeFlag(true)
                .build();
            val activeCustodyEvent2 = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(true)
                .build();

            when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(List.of(activeCustodyEvent1, activeCustodyEvent2));

            assertThatThrownBy(() -> convictionService.getCustodyKeyDateByOffenderId(999L, "POM1"))
                .isInstanceOf(SingleActiveCustodyConvictionNotFoundException.class);

        }

        @Test
        public void keyDateRetrievedFromTheActiveCustodyRecord() throws SingleActiveCustodyConvictionNotFoundException {
            val today = LocalDate.now();
            val activeCustodyEvent = aCustodyEvent(1L, new ArrayList<>())
                .toBuilder()
                .activeFlag(true)
                .build();
            activeCustodyEvent.getDisposal().getCustody().getKeyDates().add(aKeyDate("POM1", "POM Handover expected start date", today));

            val event = aCustodyEvent(2L, new ArrayList<>())
                .toBuilder()
                .activeFlag(false)
                .build();
            val terminatedDisposal = event.getDisposal().toBuilder().terminationDate(LocalDate.now()).build();
            val activeEventButTerminatedCustodyEvent = event.toBuilder().disposal(terminatedDisposal).build();

            when(eventRepository.findActiveByOffenderIdWithCustody(999L)).thenReturn(List.of(activeCustodyEvent, activeEventButTerminatedCustodyEvent));

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
    }

    @Nested class ConvictionTests {
        @BeforeEach
        public void setUp() {
            final var featureSwitches = new FeatureSwitches();
            featureSwitches.getNoms().getUpdate().setKeyDates(true);
            convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient, featureSwitches);
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
    }
}

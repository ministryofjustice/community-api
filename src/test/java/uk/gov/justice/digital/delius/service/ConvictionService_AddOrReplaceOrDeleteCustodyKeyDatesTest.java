package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.entitybuilders.EventEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.KeyDateEntityBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
public class ConvictionService_AddOrReplaceOrDeleteCustodyKeyDatesTest {

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
    private ArgumentCaptor<Map<String, LocalDate>> datesAmendedOrUpdatedArgumentCaptor;
    @Captor
    private ArgumentCaptor<Map<String, LocalDate>> datesRemovedArgumentCaptor;

    @BeforeEach
    public void setUp() {
        final var featureSwitches = new FeatureSwitches();
        featureSwitches.getNoms().getUpdate().setKeyDates(true);
        convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, spgNotificationService, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient, featureSwitches);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(88L).build());
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.of(StandardReference
                .builder()
                .codeDescription("description")
                .codeValue(code)
                .build()));
        when(offenderRepository.findByOffenderId(anyLong())).thenReturn(Optional.of(anOffender()));
    }

    @Test
    void willDeleteKeyDatesNotPresentInRequestAndAddOnesThatArePresent() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        final var custody = convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .build());

        assertThat(custody.getKeyDates().getLicenceExpiryDate()).isNull();
        assertThat(custody.getKeyDates().getConditionalReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 1));
    }
    @Test
    void willLeaveAloneKeyDatesNotPresentButNotCustodyManagedDatesInRequestAndAddOnesThatArePresent() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("POM1", "POM", LocalDate.of(2039, 9, 30))
        )))));

        final var custody = convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .build());

        assertThat(custody.getKeyDates().getConditionalReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(custody.getKeyDates().getExpectedPrisonOffenderManagerHandoverStartDate()).isEqualTo(LocalDate.of(2039, 9, 30));

    }
    @Test
    void willAddReplaceDeleteKeyDate() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.of(1995, 1, 1)),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        final var custody = convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .licenceExpiryDate(LocalDate.of(2030, 1, 2))
                .build());

        assertThat(custody.getKeyDates().getConditionalReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(custody.getKeyDates().getLicenceExpiryDate()).isEqualTo(LocalDate.of(2030, 1, 2));

    }
    @Test
    void willUpdateExistingKeyDates() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        final var custody = convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .sentenceExpiryDate(LocalDate.of(2030, 1, 1))
                .build());

        assertThat(custody.getKeyDates().getSentenceExpiryDate()).isEqualTo(LocalDate.of(2030, 1, 1));
    }

    @Test
    void willNotNotifyIAPSWhenSentenceExpiryDateChangesUpdateExistingKeyDates() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .sentenceExpiryDate(LocalDate.of(2030, 1, 1))
                .build());

        verify(iapsNotificationService, never()).notifyEventUpdated(any());
    }

    @Test
    void willNotNotifyIAPSWhenSentenceExpiryDateChangesDeletesExistingKeyDate() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.now())
        )))));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .paroleEligibilityDate(LocalDate.now())
                .build());

        verify(iapsNotificationService, never()).notifyEventUpdated(any());
    }

    @Test
    void willAddContactWithUpdatedRemovedAndDeletedDates() {
        final var event = aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.of(1995, 1, 1)),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("PED", "paroleEligibilityDate", LocalDate.of(2030, 1, 4)),
                aKeyDate("PSSED", "pssEndDate", LocalDate.of(2039, 8, 30))
        )));

        final var offender = anOffender();

        when(offenderRepository.findByOffenderId(anyLong())).thenReturn(Optional.of(offender));
        when(eventRepository.findById(88L)).thenReturn(Optional.of(event));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .licenceExpiryDate(LocalDate.of(2030, 1, 2))
                .hdcEligibilityDate(LocalDate.of(2030, 1, 3))
                .paroleEligibilityDate(LocalDate.of(2030, 1, 4))
                .expectedReleaseDate(LocalDate.of(2030, 1, 5))
                .build());


        verify(contactService).addContactForBulkCustodyKeyDateUpdate(eq(offender), eq(event), datesAmendedOrUpdatedArgumentCaptor.capture(), datesRemovedArgumentCaptor
                .capture());

        assertThat(datesAmendedOrUpdatedArgumentCaptor.getValue())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                // all new and updated
                "Conditional Release Date", LocalDate.of(2030, 1, 1),
                "Licence Expiry Date", LocalDate.of(2030, 1, 2),
                "HDC Eligibility Date", LocalDate.of(2030, 1, 3),
                "Expected Release Date", LocalDate.of(2030, 1, 5)
        ))      // not changed - so not present
                .doesNotContain(Map.entry("Parole Eligibility Date", LocalDate.of(2030, 1, 4)));

        assertThat(datesRemovedArgumentCaptor.getValue()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "Sentence Expiry Date", LocalDate.of(2039, 9, 30),
                "PSS End Date", LocalDate.of(2039, 8, 30)
        ));
    }
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void willDoNothingWhenNoDatesHaveChanged() {
        final var event = aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.of(1995, 1, 1)),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("PED", "paroleEligibilityDate", LocalDate.of(2030, 1, 4)),
                aKeyDate("PSSED", "pssEndDate", LocalDate.of(2039, 8, 30))
        )));

        when(eventRepository.findById(88L)).thenReturn(Optional.of(event));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .licenceExpiryDate(LocalDate.of(2039, 9, 30))
                .sentenceExpiryDate(LocalDate.of(2039, 9, 30))
                .paroleEligibilityDate(LocalDate.of(2030, 1, 4))
                .postSentenceSupervisionEndDate(LocalDate.of(2039, 8, 30))
                .build());


        verify(contactService, never()).addContactForBulkCustodyKeyDateUpdate(any(), any(), any(), any());
        verify(lookupSupplier, never()).userSupplier();
        verify(lookupSupplier, never()).custodyKeyDateTypeSupplier();
        verify(eventRepository, never()).save(any());
    }

    @Nested
    @MockitoSettings(strictness = Strictness.LENIENT)
    class Telemetry {
        @Captor
        private ArgumentCaptor<Map<String, String>> telemetryAttributesCaptor;

        @BeforeEach
        void setUp() {
            when(offenderRepository.findByOffenderId(anyLong())).thenReturn(Optional.of(anOffender().toBuilder().crn("X12345").nomsNumber("A1234DY").build()));
        }

        @Test
        @DisplayName("will raise no change event when nothing has changed")
        void willRaiseNoChangeEventWhenNothingHasChanged() {
            when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.of(1995, 1, 1)),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("PED", "paroleEligibilityDate", LocalDate.of(2030, 1, 4)),
                aKeyDate("PSSED", "pssEndDate", LocalDate.of(2039, 8, 30))
            ))).toBuilder().eventNumber("5").build()));

            convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .licenceExpiryDate(LocalDate.of(2039, 9, 30))
                .sentenceExpiryDate(LocalDate.of(2039, 9, 30))
                .paroleEligibilityDate(LocalDate.of(2030, 1, 4))
                .postSentenceSupervisionEndDate(LocalDate.of(2039, 8, 30))
                .build());

            verify(telemetryClient).trackEvent(eq("keyDatesBulkUnchanged"), telemetryAttributesCaptor.capture(), isNull());
            assertThat(telemetryAttributesCaptor.getValue()).contains(
                entry("eventId", "88"),
                entry("eventNumber", "5"),
                entry("crn", "X12345"),
                entry("offenderNo", "A1234DY"));
        }

        @Test
        @DisplayName("will raise all dates being removed event when all existing dates removed")
        void willRaiseAllDatesBeingRemovedEventWhenAllExistingDatesRemoved() {
            when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.parse("1995-01-01")),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.parse("2039-09-30")),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.parse("2039-10-30")),
                aKeyDate("PED", "paroleEligibilityDate", LocalDate.parse("2030-01-04")),
                aKeyDate("PSSED", "pssEndDate", LocalDate.parse("2039-08-30"))
            ))).toBuilder().eventNumber("5").build()));


            convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .build());

            verify(telemetryClient).trackEvent(eq("keyDatesBulkAllRemoved"), telemetryAttributesCaptor.capture(), isNull());
            verify(telemetryClient).trackEvent(eq("keyDatesBulkSummary"), any(), isNull());

            assertThat(telemetryAttributesCaptor.getValue()).contains(
                entry("eventId", "88"),
                entry("eventNumber", "5"),
                entry("crn", "X12345"),
                entry("offenderNo", "A1234DY"),
                entry("Licence Expiry Date", "2039-09-30"),
                entry("Sentence Expiry Date", "2039-10-30"),
                entry("Parole Eligibility Date", "2030-01-04"),
                entry("PSS End Date", "2039-08-30")
            );
        }
        @Test
        @DisplayName("will raise some dates being removed event when some existing dates removed")
        void willRaiseSomeDatesBeingRemovedEventWhenSomeExistingDatesRemoved() {
            when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.parse("1995-01-01")),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.parse("2039-09-30")),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.parse("2039-10-30")),
                aKeyDate("PED", "paroleEligibilityDate", LocalDate.parse("2030-01-04")),
                aKeyDate("PSSED", "pssEndDate", LocalDate.parse("2039-08-30"))
            ))).toBuilder().eventNumber("5").build()));


            convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .licenceExpiryDate(LocalDate.parse("2039-09-30"))
                .sentenceExpiryDate(LocalDate.parse("2039-10-30"))
                .build());

            verify(telemetryClient).trackEvent(eq("keyDatesBulkSomeRemoved"), telemetryAttributesCaptor.capture(), isNull());
            verify(telemetryClient).trackEvent(eq("keyDatesBulkSummary"), any(), isNull());

            assertThat(telemetryAttributesCaptor.getValue()).contains(
                entry("eventId", "88"),
                entry("eventNumber", "5"),
                entry("crn", "X12345"),
                entry("offenderNo", "A1234DY"),
                entry("Parole Eligibility Date", "2030-01-04"),
                entry("PSS End Date", "2039-08-30")
            );
        }

        @Test
        @DisplayName("will raise a bulk update summary event")
        void willRaiseABulkUpdateSummaryEvent() {
            when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.parse("1995-01-01")),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.parse("2039-09-30")),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.parse("2039-10-30")),
                aKeyDate("PED", "paroleEligibilityDate", LocalDate.parse("2030-01-04")),
                aKeyDate("PSSED", "pssEndDate", LocalDate.parse("2039-08-30"))
            ))).toBuilder().eventNumber("5").build()));


            convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .licenceExpiryDate(LocalDate.parse("2039-09-30"))
                .sentenceExpiryDate(LocalDate.parse("2039-10-30"))
                .paroleEligibilityDate(LocalDate.parse("2030-02-05"))
                .conditionalReleaseDate(LocalDate.parse("2029-02-05"))
                .expectedReleaseDate(LocalDate.parse("2029-02-05"))
                .build());

            verify(telemetryClient).trackEvent(eq("keyDatesBulkSummary"), telemetryAttributesCaptor.capture(), isNull());

            assertThat(telemetryAttributesCaptor.getValue()).contains(
                entry("eventId", "88"),
                entry("eventNumber", "5"),
                entry("crn", "X12345"),
                entry("offenderNo", "A1234DY"),
                entry("updated", "1"), // PED
                entry("inserted", "2"), //CRD, ERD
                entry("deleted", "1"), // PSS
                entry("unchanged", "2") // LED, SED
            );
        }
    }
}

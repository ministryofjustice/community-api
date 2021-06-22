package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;
import uk.gov.justice.digital.delius.data.api.UpdateCustodyBookingNumber;
import uk.gov.justice.digital.delius.jpa.standard.entity.CustodyHistory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.CustodyHistoryRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository.DuplicateOffenderException;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.aPrisonInstitution;
import static uk.gov.justice.digital.delius.util.EntityHelper.aPrisonOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;
import static uk.gov.justice.digital.delius.util.EntityHelper.anInstitution;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

public class CustodyServiceTest {
    private CustodyService custodyService;

    private final TelemetryClient telemetryClient = mock(TelemetryClient.class);
    private final OffenderRepository offenderRepository = mock(OffenderRepository.class);
    private final ConvictionService convictionService = mock(ConvictionService.class);
    private final InstitutionRepository institutionRepository = mock(InstitutionRepository.class);
    private final CustodyHistoryRepository custodyHistoryRepository = mock(CustodyHistoryRepository.class);
    private final ReferenceDataService referenceDataService = mock(ReferenceDataService.class);
    private final SpgNotificationService spgNotificationService = mock(SpgNotificationService.class);
    private final ArgumentCaptor<CustodyHistory> custodyHistoryArgumentCaptor = ArgumentCaptor.forClass(CustodyHistory.class);
    private final OffenderManagerService offenderManagerService = mock(OffenderManagerService.class);
    private final ContactService contactService = mock(ContactService.class);
    private final OffenderPrisonerService offenderPrisonerService = mock(OffenderPrisonerService.class);

    @BeforeEach
    public void setup() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
        final var featureSwitches = new FeatureSwitches();
        featureSwitches.getNoms().getUpdate().setCustody(true);
        featureSwitches.getNoms().getUpdate().getBooking().setNumber(true);
        custodyService = new CustodyService(telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService, featureSwitches);
        when(offenderRepository.findByNomsNumber(anyString())).thenReturn(Optional.of(Offender.builder().offenderId(99L).build()));
        when(convictionService.getAllActiveCustodialEvents(anyLong()))
                .thenReturn(List.of(EntityHelper.aCustodyEvent()));
    }

    @Nested
    class WhenUpdatingCustodyForTransfer {
        private final ArgumentMatcher<Map<String, String>> standardTelemetryAttributes =
                attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
                        Optional.ofNullable(attributes.get("bookingNumber")).filter(value -> value.equals("44463B")).isPresent() &&
                        Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("MDI")).isPresent();

        @BeforeEach
        public void setup() {
            when(referenceDataService.getPrisonLocationChangeCustodyEvent()).thenReturn(StandardReference.builder().codeValue("CPL").codeDescription("Change prison location").build());
            when(referenceDataService.getCustodyStatusChangeCustodyEvent()).thenReturn(StandardReference.builder().codeValue("TSC").codeDescription("Custody status change").build());
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));
            when(offenderRepository.findMostLikelyByNomsNumber(anyString())).thenReturn(Either.right(Optional.of(Offender.builder().offenderId(99L).build())));
        }


        @Test
        public void willCreateTelemetryEventWhenOffenderNotFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.empty()));

            assertThatThrownBy(() ->
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

            verify(telemetryClient).trackEvent(eq("P2PTransferOffenderNotFound"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willThrowExceptionWhenOffenderNotFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.empty()));

            assertThatThrownBy(() ->
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);

        }

        @Test
        public void willCreateTelemetryEventWhenMultipleActiveOffendersFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.left(new OffenderRepository.DuplicateOffenderException("Two found!")));

            assertThatThrownBy(() ->
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

            verify(telemetryClient).trackEvent(eq("P2PTransferMultipleOffendersFound"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willThrowExceptionWhenMultipleOffendersFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.left(new OffenderRepository.DuplicateOffenderException("Two found!")));

            assertThatThrownBy(() ->
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);

        }

        @Test
        public void willCreateTelemetryEventWhenConvictionNotFound() {
            when(convictionService.getAllActiveCustodialEvents(99L)).thenReturn(List.of());

            assertThatThrownBy(() ->
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

            verify(telemetryClient).trackEvent(eq("P2PTransferBookingNumberNotFound"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willThrowExceptionWhenBookingNumberNotFound() {
            when(convictionService.getAllActiveCustodialEvents(99L)).thenReturn(List.of());

            assertThatThrownBy(() ->
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);
        }


        @Nested
        @DisplayName("With multiple active custodial events")
        class WithMultipleActiveCustodialEvents {
            final LocalDate latestEventSentenceStartDate = LocalDate.now();
            @BeforeEach
            void setUp() {
                when(convictionService.getAllActiveCustodialEvents(anyLong()))
                    .thenReturn(List.of(EntityHelper.aCustodyEvent(latestEventSentenceStartDate), EntityHelper.aCustodyEvent(LocalDate.now().minusYears(1))));

            }

            @Nested
            class WhenAllowMultipleEventUpdateFeatureSwitchOn {
                @BeforeEach
                void setUp() {
                    final var featureSwitches = new FeatureSwitches();
                    featureSwitches.getNoms().getUpdate().setCustody(true);
                    featureSwitches.getNoms().getUpdate().getMultipleEvents().setUpdatePrisonLocation(true);
                    custodyService = new CustodyService(telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService, featureSwitches);
                }

                @Test
                @DisplayName("will create a single telemetry event for all records updated")
                public void willCreateSingleTelemetryEventWhenMultipleConvictionsAreUpdated() {
                    final ArgumentMatcher<Map<String, String>> telemetryAttributes =
                        attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
                            Optional.ofNullable(attributes.get("bookingNumber")).filter(value -> value.equals("44463B")).isPresent() &&
                            Optional.ofNullable(attributes.get("updatedCount")).filter(value -> value.equals("2")).isPresent() &&
                            Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("MDI")).isPresent();

                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

                    verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdated"), argThat(telemetryAttributes), isNull());
                }

                @Test
                @DisplayName("will return the latest updated custody record")
                public void willReturnLatestUpdatedEvent() {
                    when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

                    final var updatedCustody = custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

                    assertThat(updatedCustody.getInstitution().getDescription()).isEqualTo("HMP Highland");
                    assertThat(updatedCustody.getSentenceStartDate()).isEqualTo(latestEventSentenceStartDate);
                }

                @Test
                @DisplayName("will create custodial history record for each event updated")
                public void willCreateCustodyHistoryChangeLocationForEachEvent() {
                    when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

                    verify(custodyHistoryRepository, times(2)).save(custodyHistoryArgumentCaptor.capture());

                    final var custodyHistoryEvent = custodyHistoryArgumentCaptor.getValue();

                    assertThat(custodyHistoryEvent.getCustodyEventType().getCodeValue()).isEqualTo("CPL");
                    assertThat(custodyHistoryEvent.getDetail()).isEqualTo("HMP Highland");
                    assertThat(custodyHistoryEvent.getWhen()).isEqualTo(LocalDate.now());
                }

                @Test
                @DisplayName("will notify SPG for each event updated")
                public void willNotifySPGOfCustodyChangeForEachEvent() {
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

                    verify(spgNotificationService, times(2)).notifyUpdateOfCustody(any(), any());
                }

                @Test
                @DisplayName("will create a contact for each event updated")
                public void willCreateContactAboutPrisonLocationChangeForEachEvent() {
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

                    verify(contactService, times(2)).addContactForPrisonLocationChange(any(), any());
                }

                @Test
                @DisplayName("will ignore events with the incorrect status but others will be updated")
                public void willUpdateOneEventEvenWhenTheOtherIsInWrongStatus() {
                    when(convictionService.getAllActiveCustodialEvents(anyLong()))
                        .thenReturn(List.of(
                            EntityHelper.aCustodyEvent(),
                            EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("B").codeDescription("Released on Licence").build())));


                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

                    verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdated"), argThat(standardTelemetryAttributes), isNull());
                    verify(spgNotificationService).notifyUpdateOfCustody(any(), any());
                    verify(contactService).addContactForPrisonLocationChange(any(), any());
                    verify(custodyHistoryRepository).save(any());
                }
            }

            @Nested
            class WhenAllowMultipleEventUpdateFeatureSwitchOff {
                @BeforeEach
                void setUp() {
                    final var featureSwitches = new FeatureSwitches();
                    featureSwitches.getNoms().getUpdate().setCustody(true);
                    featureSwitches.getNoms().getUpdate().getMultipleEvents().setUpdatePrisonLocation(false);
                    custodyService = new CustodyService(telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService, featureSwitches);
                }

                @Test
                @DisplayName("will throw an exception and create a single telemetry event")
                public void willCreateSingleTelemetryEventWhenMultipleConvictionsAreUpdated() {
                    final ArgumentMatcher<Map<String, String>> telemetryAttributes =
                        attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
                            Optional.ofNullable(attributes.get("bookingNumber")).filter(value -> value.equals("44463B")).isPresent() &&
                            Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("MDI")).isPresent();

                    assertThatThrownBy(() -> custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build())).isInstanceOf(NotFoundException.class);

                    verify(telemetryClient).trackEvent(eq("P2PTransferBookingNumberHasDuplicates"), argThat(telemetryAttributes), isNull());
                }

                @Test
                @DisplayName("will not create custodial history record for any event")
                public void willCreateCustodyHistoryChangeLocationForEachEvent() {
                    assertThatThrownBy(() -> custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

                    verify(custodyHistoryRepository, never()).save(custodyHistoryArgumentCaptor.capture());
                }

                @Test
                @DisplayName("will not notify SPG for each any event")
                public void willNotifySPGOfCustodyChangeForEachEvent() {
                    assertThatThrownBy(() -> custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

                    verify(spgNotificationService, never()).notifyUpdateOfCustody(any(), any());
                }

                @Test
                @DisplayName("will not create a contact for any event")
                public void willCreateContactAboutPrisonLocationChangeForEachEvent() {
                    assertThatThrownBy(() -> custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

                    verify(contactService, never()).addContactForPrisonLocationChange(any(), any());
                }
            }

        }

        @Test
        public void willCreateTelemetryEventWhenPrisonNotFound() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

            verify(telemetryClient).trackEvent(eq("P2PTransferPrisonNotFound"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willThrowExceptionWhenPrisonNotFound() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        public void willCreateTelemetryEventAndNothingElseWhenPrisonAlreadySet() {
            final var newInstitution = aPrisonInstitution();
            final var currentInstitution = aPrisonInstitution();
            final var event = EntityHelper.aCustodyEvent();
            event.getDisposal().getCustody().setInstitution(currentInstitution);
            when(convictionService.getAllActiveCustodialEvents(anyLong())).thenReturn(List
                .of(event));

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(newInstitution));


            final var custody = custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdateIgnored"), argThat(standardTelemetryAttributes), isNull());

            verify(spgNotificationService, never()).notifyUpdateOfCustody(any(), any());
            verify(contactService, never()).addContactForPrisonLocationChange(any(), any());
            verify(offenderManagerService, never()).autoAllocatePrisonOffenderManagerAtInstitution(any(), any());

            assertThat(custody).isNotNull();
        }

        @Test
        public void willCreateTelemetryEventWhenPrisonLocationChanges() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution()));

            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdated"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willCreateCustodyHistoryChangeLocationEvent() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));
            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(custodyHistoryRepository).save(custodyHistoryArgumentCaptor.capture());

            final var custodyHistoryEvent = custodyHistoryArgumentCaptor.getValue();

            assertThat(custodyHistoryEvent.getCustodyEventType().getCodeValue()).isEqualTo("CPL");
            assertThat(custodyHistoryEvent.getDetail()).isEqualTo("HMP Highland");
            assertThat(custodyHistoryEvent.getWhen()).isEqualTo(LocalDate.now());
        }

        @Test
        public void willNotifySPGOfCustodyChange() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(spgNotificationService).notifyUpdateOfCustody(any(), any());
        }

        @Test
        public void willCreateContactAboutPrisonLocationChange() {
            final var offender = anOffender();
            final var event = aCustodyEvent();
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.of(offender)));
            when(convictionService.getAllActiveCustodialEvents(anyLong())).thenReturn(List.of(event));

            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(contactService).addContactForPrisonLocationChange(offender, event);
        }

        @Test
        public void willCreateNewPrisonOffenderManagerWhenExistingPOMAtDifferentPrison() {
            final var offender = Offender.builder().offenderId(99L).build();
            final var institution = anInstitution();

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(institution));
            when(offenderManagerService.isPrisonOffenderManagerAtInstitution(any(), any())).thenReturn(false);

            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(offenderManagerService).autoAllocatePrisonOffenderManagerAtInstitution(offender, institution);
        }

        @Test
        public void willNotCreateNewPrisonOffenderManagerWhenExistingPOMAtSamePrison() {
            final var institution = anInstitution();

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(institution));
            when(offenderManagerService.isPrisonOffenderManagerAtInstitution(any(), any())).thenReturn(true);

            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(offenderManagerService, never()).autoAllocatePrisonOffenderManagerAtInstitution(any(), any());
        }

        @Test
        public void willCreateCustodyHistoryChangeCustodyStatusWhenCurrentlyOnlySentenced() {
            when(convictionService.getAllActiveCustodialEvents(anyLong()))
                .thenReturn(List.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("A").codeDescription("Sentenced in custody").build())));

            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(custodyHistoryRepository, times(2)).save(custodyHistoryArgumentCaptor.capture());

            final var custodyHistoryEvent = custodyHistoryArgumentCaptor.getAllValues()
                    .stream()
                    .filter(history -> history.getCustodyEventType().getCodeValue().equals("TSC"))
                    .findAny()
                    .orElseThrow();

            assertThat(custodyHistoryEvent.getCustodyEventType().getCodeValue()).isEqualTo("TSC");
            assertThat(custodyHistoryEvent.getDetail()).isEqualTo("DSS auto update in custody");
            assertThat(custodyHistoryEvent.getWhen()).isEqualTo(LocalDate.now());
        }

        @Test
        public void willGetInCustodyStatusWhenCurrentlyOnlySentenced() {
            when(convictionService.getAllActiveCustodialEvents(anyLong()))
                .thenReturn(List.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("A").codeDescription("Sentenced in custody").build())));

            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(referenceDataService).getInCustodyCustodyStatus();
        }

        @Test
        public void willNotifySPGOfCustodyLocationChangeWhenCurrentlyOnlySentenced() {
            when(convictionService.getAllActiveCustodialEvents(anyLong()))
                .thenReturn(List.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("A").codeDescription("Sentenced in custody").build())));

            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(spgNotificationService).notifyUpdateOfCustodyLocationChange(any(), any());
        }

        @Test
        public void willNotifySPGOfCustodyLocationChangeWhenCurrentlyInCustody() {
            when(convictionService.getAllActiveCustodialEvents(anyLong()))
                .thenReturn(List.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("D").codeDescription("In Custody").build())));

            custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(spgNotificationService).notifyUpdateOfCustodyLocationChange(any(), any());
        }

        @Test
        public void willCreateTelemetryEventWhenPrisonLocationChangesButStatusNotCurrentlyInPrison() {
            when(convictionService.getAllActiveCustodialEvents(anyLong()))
                .thenReturn(List.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("B").codeDescription("Released on Licence").build())));

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

            assertThatThrownBy(() ->
                    custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);

            verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdateIgnored"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willUpdatePrisonInstitutionWillBeUpdatedWhenFeatureSwitchedOn() {
            final var featureSwitches = new FeatureSwitches();
            featureSwitches.getNoms().getUpdate().setCustody(true);
            featureSwitches.getNoms().getUpdate().getBooking().setNumber(true);

            custodyService = new CustodyService(telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService, featureSwitches);

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

            final var updatedCustody = custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            assertThat(updatedCustody.getInstitution().getDescription()).isEqualTo("HMP Highland");
        }

        @Test
        public void willNotUpdatePrisonInstitutionWillBeUpdatedWhenFeatureSwitchedOff() {
            final var featureSwitches = new FeatureSwitches();
            featureSwitches.getNoms().getUpdate().setCustody(false);
            featureSwitches.getNoms().getUpdate().getBooking().setNumber(true);
            custodyService = new CustodyService(telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService, featureSwitches);

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

            final var updatedCustody = custodyService.updateCustodyPrisonLocation("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            assertThat(updatedCustody.getInstitution().getDescription()).isNotEqualTo("HMP Highland");
        }
    }
    @Nested
    class WhenUpdatingCustodyForPOMAllocation {
        private final ArgumentMatcher<Map<String, String>> standardTelemetryAttributes =
                attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
                        Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("MDI")).isPresent();

        @BeforeEach
        public void setup() {
            when(referenceDataService.getPrisonLocationChangeCustodyEvent()).thenReturn(StandardReference.builder().codeValue("CPL").codeDescription("Change prison location").build());
            when(referenceDataService.getCustodyStatusChangeCustodyEvent()).thenReturn(StandardReference.builder().codeValue("TSC").codeDescription("Custody status change").build());
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(aPrisonInstitution()));
            when(offenderRepository.findMostLikelyByNomsNumber(anyString())).thenReturn(Either.right(Optional.of(Offender.builder().offenderId(99L).prisonOffenderManagers(List.of(aPrisonOffenderManager(aStaff(), aTeam()))).build())));
        }


        @Test
        @DisplayName("will add not found telemetry when offender not found")
        public void willCreateTelemetryEventWhenOffenderNotFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.empty()));

            custodyService.updateCustodyPrisonLocation("G9542VP", "MDI");

            verify(telemetryClient).trackEvent(eq("POMLocationOffenderNotFound"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        @DisplayName("will add duplicate offender telemetry when multiple offenders found")
        public void willCreateTelemetryEventWhenMultipleOffendersFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.left(new OffenderRepository.DuplicateOffenderException("Two have been found")));

            custodyService.updateCustodyPrisonLocation("G9542VP", "MDI");

            verify(telemetryClient).trackEvent(eq("POMLocationMultipleOffenders"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        @DisplayName("will add event not found telemetry when no active custodial events found")
        public void willCreateTelemetryEventWhenConvictionNotFound() {
            when(convictionService.getAllActiveCustodialEvents(99L)).thenReturn(List.of());

            custodyService.updateCustodyPrisonLocation("G9542VP", "MDI");

            verify(telemetryClient).trackEvent(eq("POMLocationNoEvents"), argThat(standardTelemetryAttributes), isNull());
        }


        @Test
        @DisplayName("will add missing prison telemetry when prison not found")
        public void willCreateTelemetryEventWhenPrisonNotFound() {
            final var event = EntityHelper.aCustodyEvent();
            event.getDisposal().getCustody().setInstitution(aPrisonInstitution());
            when(convictionService.getAllActiveCustodialEvents(99L)).thenReturn(List.of(event));
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.empty());

            custodyService.updateCustodyPrisonLocation("G9542VP", "MDI");

            verify(telemetryClient).trackEvent(eq("POMLocationPrisonNotFound"), argThat(standardTelemetryAttributes), isNull());
        }


        @Test
        @DisplayName("will add a no change telemetry when location already correct")
        public void willCreateTelemetryEventAndNothingElseWhenPrisonAlreadySet() {
            final var event = EntityHelper.aCustodyEvent();
            event.getDisposal().getCustody().setInstitution(aPrisonInstitution());
            when(convictionService.getAllActiveCustodialEvents(99L)).thenReturn(List.of(event));
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(aPrisonInstitution()));


            custodyService.updateCustodyPrisonLocation("G9542VP", "MDI");

            verify(telemetryClient).trackEvent(eq("POMLocationCorrect"), argThat(standardTelemetryAttributes), isNull());

            verify(spgNotificationService, never()).notifyUpdateOfCustody(any(), any());
            verify(contactService, never()).addContactForPrisonLocationChange(any(), any());
            verify(offenderManagerService, never()).autoAllocatePrisonOffenderManagerAtInstitution(any(), any());
        }
        @Test
        @DisplayName("will add a no change telemetry when custodial status indicates they are not in prison")
        public void willCreateTelemetryEventWhenPrisonLocationChangesButStatusNotCurrentlyInPrison() {
            final var event = EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("B").codeDescription("Released on Licence").build());
            event.getDisposal().getCustody().setInstitution(aPrisonInstitution());
            when(convictionService.getAllActiveCustodialEvents(99L)).thenReturn(List.of(event));
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

            custodyService.updateCustodyPrisonLocation("G9542VP", "MDI");

            verify(telemetryClient).trackEvent(eq("POMLocationCustodialStatusNotCorrect"), argThat(standardTelemetryAttributes), isNull());
        }

        @Nested
        class OnSuccessfulChangeWithMultipleCustodialEvents {
            @Nested
            class WhenAllowMultipleEventUpdateFeatureSwitchOn {
                @BeforeEach
                void setUp() {
                    final var featureSwitches = new FeatureSwitches();
                    featureSwitches.getNoms().getUpdate().setCustody(true);
                    featureSwitches.getNoms().getUpdate().getMultipleEvents().setUpdatePrisonLocation(true);
                    custodyService = new CustodyService(telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService, featureSwitches);

                    final var event1 = EntityHelper.aCustodyEvent();
                    event1.getDisposal().getCustody().setInstitution(aPrisonInstitution().toBuilder().nomisCdeCode("MDI").build());
                    final var event2 = EntityHelper.aCustodyEvent();
                    event2.getDisposal().getCustody().setInstitution(aPrisonInstitution().toBuilder().nomisCdeCode("MDI").build());
                    when(convictionService.getAllActiveCustodialEvents(99L)).thenReturn(List.of(event1, event2));
                    when(institutionRepository.findByNomisCdeCode("WWI")).thenReturn(Optional.of(aPrisonInstitution()));
                    when(offenderManagerService.isPrisonOffenderManagerAtInstitution(any(), any())).thenCallRealMethod();

                    custodyService.updateCustodyPrisonLocation("G9542VP", "WWI");
                }

                @Test
                @DisplayName("will add a single location change telemetry when location changes")
                public void willCreateTelemetryEventWhenMultipleConvictionsFound() {
                    final ArgumentMatcher<Map<String, String>> telemetryAttributes =
                        attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
                            Optional.ofNullable(attributes.get("updatedCount")).filter(value -> value.equals("2")).isPresent() &&
                            Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("WWI")).isPresent();

                    verify(telemetryClient).trackEvent(eq("POMLocationUpdated"), argThat(telemetryAttributes), isNull());
                }

                @Test
                @DisplayName("will created custody history record for each event updated")
                public void willCreateCustodyHistoryChangeLocationEvent() {
                    verify(custodyHistoryRepository, times(2)).save(custodyHistoryArgumentCaptor.capture());
                }

                @Test
                @DisplayName("will notify SPG of change for each event")
                public void willNotifySPGOfCustodyChange() {
                    verify(spgNotificationService, times(2)).notifyUpdateOfCustody(any(), any());
                }

                @Test
                @DisplayName("will create contact for each event")
                public void willCreateContactAboutPrisonLocationChange() {
                    verify(contactService, times(2)).addContactForPrisonLocationChange(any(), any());
                }
            }
            @Nested
            class WhenAllowMultipleEventUpdateFeatureSwitchOff {
                @BeforeEach
                void setUp() {
                    final var featureSwitches = new FeatureSwitches();
                    featureSwitches.getNoms().getUpdate().setCustody(true);
                    featureSwitches.getNoms().getUpdate().getMultipleEvents().setUpdatePrisonLocation(false);
                    custodyService = new CustodyService(telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService, featureSwitches);

                    final var event1 = EntityHelper.aCustodyEvent();
                    event1.getDisposal().getCustody().setInstitution(aPrisonInstitution().toBuilder().nomisCdeCode("MDI").build());
                    final var event2 = EntityHelper.aCustodyEvent();
                    event2.getDisposal().getCustody().setInstitution(aPrisonInstitution().toBuilder().nomisCdeCode("MDI").build());
                    when(convictionService.getAllActiveCustodialEvents(99L)).thenReturn(List.of(event1, event2));
                    when(institutionRepository.findByNomisCdeCode("WWI")).thenReturn(Optional.of(aPrisonInstitution()));
                    when(offenderManagerService.isPrisonOffenderManagerAtInstitution(any(), any())).thenCallRealMethod();

                    custodyService.updateCustodyPrisonLocation("G9542VP", "WWI");
                }

                @Test
                @DisplayName("will add a single location change telemetry when location changes")
                public void willCreateTelemetryEventWhenMultipleConvictionsFound() {
                    final ArgumentMatcher<Map<String, String>> telemetryAttributes =
                        attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
                            Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("WWI")).isPresent();

                    verify(telemetryClient).trackEvent(eq("POMLocationMultipleEvents"), argThat(telemetryAttributes), isNull());
                }

                @Test
                @DisplayName("will not create custody history record for any event")
                public void willCreateCustodyHistoryChangeLocationEvent() {
                    verify(custodyHistoryRepository, never()).save(custodyHistoryArgumentCaptor.capture());
                }

                @Test
                @DisplayName("will not notify SPG of change for any event")
                public void willNotifySPGOfCustodyChange() {
                    verify(spgNotificationService, never()).notifyUpdateOfCustody(any(), any());
                }

                @Test
                @DisplayName("will not create contact for any  event")
                public void willCreateContactAboutPrisonLocationChange() {
                    verify(contactService, never()).addContactForPrisonLocationChange(any(), any());
                }
            }
        }

        @Nested
        class OnSuccessfulChange {
            @BeforeEach
            void setUp() {
                final var event = EntityHelper.aCustodyEvent();
                event.getDisposal().getCustody().setInstitution(aPrisonInstitution().toBuilder().nomisCdeCode("MDI").build());
                when(convictionService.getAllActiveCustodialEvents(99L)).thenReturn(List.of(event));
                when(institutionRepository.findByNomisCdeCode("WWI")).thenReturn(Optional.of(aPrisonInstitution()));
                when(offenderManagerService.isPrisonOffenderManagerAtInstitution(any(), any())).thenCallRealMethod();

                custodyService.updateCustodyPrisonLocation("G9542VP", "WWI");
            }

            @Test
            @DisplayName("will add a location change telemetry when location changes")
            public void willCreateTelemetryEventWhenPrisonLocationChanges() {
                final ArgumentMatcher<Map<String, String>> standardTelemetryAttributes =
                        attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
                                Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("WWI")).isPresent();

                verify(telemetryClient).trackEvent(eq("POMLocationUpdated"), argThat(standardTelemetryAttributes), isNull());
            }

            @Test
            @DisplayName("will created custody history record")
            public void willCreateCustodyHistoryChangeLocationEvent() {
                verify(custodyHistoryRepository).save(custodyHistoryArgumentCaptor.capture());

                final var custodyHistoryEvent = custodyHistoryArgumentCaptor.getValue();

                assertThat(custodyHistoryEvent.getCustodyEventType().getCodeValue()).isEqualTo("CPL");
                assertThat(custodyHistoryEvent.getWhen()).isEqualTo(LocalDate.now());
            }

            @Test
            @DisplayName("will notify SPG of change")
            public void willNotifySPGOfCustodyChange() {
                verify(spgNotificationService).notifyUpdateOfCustody(any(), any());
            }

            @Test
            @DisplayName("will create contact")
            public void willCreateContactAboutPrisonLocationChange() {
                verify(contactService).addContactForPrisonLocationChange(any(), any());
            }

            @Test
            @DisplayName("will not allocate a POM")
            public void willCreateNewPrisonOffenderManagerWhenExistingPOMAtDifferentPrison() {
                verify(offenderManagerService, never()).autoAllocatePrisonOffenderManagerAtInstitution(any(), any());
            }

        }

    }

    @Nested
    class WhenUpdatingCustodyBookingNumber {
        private final ArgumentMatcher<Map<String, String>> standardTelemetryAttributes =
                attributes ->  Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
                        Optional.ofNullable(attributes.get("bookingNumber")).filter(value -> value.equals("44463B")).isPresent() &&
                        Optional.ofNullable(attributes.get("sentenceStartDate")).filter(value -> value.equals("2020-02-28")).isPresent();

         private final UpdateCustodyBookingNumber updateCustodyBookingNumber = UpdateCustodyBookingNumber
                 .builder()
                 .sentenceStartDate(LocalDate.of(2020, 2, 28))
                 .bookingNumber("44463B")
                 .build();

        @Nested
        class WhenMultipleBookingsFound {
            @BeforeEach
            public void setup() {
                when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.left(new DuplicateOffenderException("dup")));
            }

            @Test
            public void willCreateTelemetryEventWhenMultipleBookingsFound() {
                assertThatThrownBy(() ->
                    custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber));

                verify(telemetryClient).trackEvent(eq("P2PImprisonmentStatusOffenderMultipleBookings"), argThat(standardTelemetryAttributes), isNull());
            }

            @Test
            public void willThrowExceptionWhenMultipleBookingsFound() {
                assertThatThrownBy(() ->
                    custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber))
                    .isInstanceOf(DuplicateOffenderException.class);
            }
        }

        @Nested
        class WhenOffenderNotFound {
            @BeforeEach
            public void setup() {
                when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.empty()));
            }

            @Test
            public void willCreateTelemetryEventWhenOffenderNotFound() {
                assertThatThrownBy(() ->
                        custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber));

                verify(telemetryClient).trackEvent(eq("P2PImprisonmentStatusOffenderNotFound"), argThat(standardTelemetryAttributes), isNull());
            }

            @Test
            public void willThrowExceptionWhenOffenderNotFound() {
                assertThatThrownBy(() ->
                        custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber))
                        .isInstanceOf(NotFoundException.class);
            }
        }

        @Nested
        class WhenDuplicateConvictionsFound {
            @BeforeEach
            public void setup() {
                when(offenderRepository.findMostLikelyByNomsNumber(anyString())).thenReturn(Either.right(Optional.of(Offender.builder().offenderId(99L).build())));
                when(convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(anyLong(), any()))
                        .thenReturn(Result.ofError(new ConvictionService.DuplicateConvictionsForSentenceDateException(2)));
            }

            @Test
            public void willCreateTelemetryEventWhenDuplicateConvictionsFound() {
                assertThatThrownBy(() ->
                        custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber));

                verify(telemetryClient).trackEvent(eq("P2PImprisonmentStatusCustodyEventsHasDuplicates"), argThat(standardTelemetryAttributes), isNull());
            }

            @Test
            public void willThrowExceptionWhenDuplicateConvictionsFound() {
                assertThatThrownBy(() ->
                        custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber))
                        .isInstanceOf(NotFoundException.class);
            }


        }

        @Nested
        class WhenConvictionNotFound {
            @BeforeEach
            public void setup() {
                when(offenderRepository.findMostLikelyByNomsNumber(anyString())).thenReturn(Either.right(Optional.of(Offender.builder().offenderId(99L).build())));
                when(convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(anyLong(), any()))
                        .thenReturn(Result.of(Optional.empty()));
            }

            @Test
            public void willCreateTelemetryEventWhenDuplicateConvictionsFound() {
                assertThatThrownBy(() ->
                        custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber));

                verify(telemetryClient).trackEvent(eq("P2PImprisonmentStatusCustodyEventNotFound"), argThat(standardTelemetryAttributes), isNull());
            }

            @Test
            public void willThrowExceptionWhenDuplicateConvictionsFound() {
                assertThatThrownBy(() ->
                        custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber))
                        .isInstanceOf(NotFoundException.class);
            }
        }

        @Nested
        class WhenCanUpdateBooking {
            private final UpdateCustodyBookingNumber updateCustodyBookingNumber = UpdateCustodyBookingNumber
                    .builder()
                    .sentenceStartDate(LocalDate.of(2020, 2, 28))
                    .bookingNumber("44463B")
                    .build();
            private final Offender offender = anOffender();
            private final Event event = aCustodyEvent();

            @BeforeEach
            void setup() {
                when(convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(anyLong(), any()))
                        .thenReturn(Result.of(Optional.of(event)));
                when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.of(offender)));
            }

            @Test
            void bookingNumberWillBeUpdated() {
                final var custody = custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

                assertThat(custody.getBookingNumber()).isEqualTo("44463B");
            }

            @Test
            void offenderPrisonersWillBeRefreshedForOffender() {
                custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

                verify(offenderPrisonerService).refreshOffenderPrisonersFor(offender);
            }

            @Test
            void spgWillBeNotifiedOfEventChange() {
                custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

                verify(spgNotificationService).notifyUpdateOfCustody(offender, event);
            }

            @Test
            void contactWillBeAddedToNotifiedOfBookingNumberChange() {
                custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

                verify(contactService).addContactForBookingNumberUpdate(offender, event);
            }

            @Nested
            class WhenFeatureSwitchedOff {
                @BeforeEach
                void setup() {
                    final var featureSwitches = new FeatureSwitches();
                    featureSwitches.getNoms().getUpdate().setCustody(true);
                    featureSwitches.getNoms().getUpdate().getBooking().setNumber(false);
                    custodyService = new CustodyService(telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService, featureSwitches);
                }

                @Test
                void nothingWillBeUpdated() {
                    final var custody = custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

                    verify(offenderPrisonerService, never()).refreshOffenderPrisonersFor(any());
                    verify(spgNotificationService, never()).notifyUpdateOfCustody(any(), any());
                    verify(contactService, never()).addContactForBookingNumberUpdate(any(), any());

                    assertThat(custody.getBookingNumber()).isNotEqualTo("44463B");
                }
            }
            @Nested
            class WhenBookingNumberAlreadySet {
                @BeforeEach
                void setup() {
                    final var custody = aCustodyEvent().getDisposal().getCustody().toBuilder().prisonerNumber("44463B").build();
                    final var disposal = aCustodyEvent().getDisposal().toBuilder().custody(custody).build();
                    when(convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(anyLong(), any()))
                            .thenReturn(Result.of(Optional.of(aCustodyEvent().toBuilder().disposal(disposal).build())));
                }

                @Test
                void nothingWillBeUpdated() {
                    final var custody = custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

                    verify(offenderPrisonerService, never()).refreshOffenderPrisonersFor(any());
                    verify(spgNotificationService, never()).notifyUpdateOfCustody(any(), any());
                    verify(contactService, never()).addContactForBookingNumberUpdate(any(), any());

                    assertThat(custody.getBookingNumber()).isEqualTo("44463B");
                }

                @Test
                public void willCreateTelemetryEventStatingAlreadySet() {
                    custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

                    verify(telemetryClient).trackEvent(eq("P2PImprisonmentStatusBookingNumberAlreadySet"), argThat(standardTelemetryAttributes), isNull());
                }
            }
            @Nested
            class WhenBookingNumberOverwritten {
                @BeforeEach
                void setup() {
                    final var custody = aCustodyEvent().getDisposal().getCustody().toBuilder().prisonerNumber("99999X").build();
                    final var disposal = aCustodyEvent().getDisposal().toBuilder().custody(custody).build();
                    when(convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(anyLong(), any()))
                            .thenReturn(Result.of(Optional.of(aCustodyEvent().toBuilder().disposal(disposal).build())));
                }

                @Test
                public void willCreateTelemetryEventStatingItIsHasBeenUpdated() {
                    custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

                    verify(telemetryClient).trackEvent(eq("P2PImprisonmentStatusBookingNumberUpdated"), argThat(standardTelemetryAttributes), isNull());
                }
            }
            @Nested
            class WhenBookingNumberInserted {
                @BeforeEach
                void setup() {
                    final var custody = aCustodyEvent().getDisposal().getCustody().toBuilder().prisonerNumber(null).build();
                    final var disposal = aCustodyEvent().getDisposal().toBuilder().custody(custody).build();
                    when(convictionService.getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(anyLong(), any()))
                            .thenReturn(Result.of(Optional.of(aCustodyEvent().toBuilder().disposal(disposal).build())));
                }

                @Test
                public void willCreateTelemetryEventStatingNewOneHasBeenInserted() {
                    custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

                    verify(telemetryClient).trackEvent(eq("P2PImprisonmentStatusBookingNumberInserted"), argThat(standardTelemetryAttributes), isNull());
                }
            }
        }
    }

    @Nested
    @DisplayName("when calling getCustodyByBookNumber")
    class WhenRetrievingByBookingNumber {
        @Nested
        @DisplayName("and there is more than one event for the booking number")
        class WhenHasMultipleActiveEvents {
            @BeforeEach
            void setup() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
                when(offenderRepository.findByNomsNumber(anyString())).thenReturn(Optional.of(Offender.builder().offenderId(99L).build()));
                when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                        .thenThrow(new ConvictionService.DuplicateActiveCustodialConvictionsException(2));
            }

            @Test
            @DisplayName("then a NotFoundException will be thrown")
            void willThrowNotFound() {
                assertThatThrownBy(() -> custodyService.getCustodyByBookNumber("G9542VP", "44463B"))
                        .isInstanceOf(NotFoundException.class);
            }
        }
        @Nested
        @DisplayName("and there no active events for the booking number")
        class WhenNoActiveEvents {
            @BeforeEach
            void setup() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
                when(offenderRepository.findByNomsNumber(anyString())).thenReturn(Optional.of(Offender.builder().offenderId(99L).build()));
                when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                        .thenReturn(Optional.empty());
            }

            @Test
            @DisplayName("then a NotFoundException will be thrown")
            void willThrowNotFound() {
                assertThatThrownBy(() -> custodyService.getCustodyByBookNumber("G9542VP", "44463B"))
                        .isInstanceOf(NotFoundException.class);
            }
        }
        @Nested
        @DisplayName("and the offender is not found")
        class WhenNoOffenderFound {
            @BeforeEach
            void setup() {
                when(offenderRepository.findByNomsNumber(anyString())).thenReturn(Optional.empty());
            }

            @Test
            @DisplayName("then a NotFoundException will be thrown")
            void willThrowNotFound() {
                assertThatThrownBy(() -> custodyService.getCustodyByBookNumber("G9542VP", "44463B"))
                        .isInstanceOf(NotFoundException.class);
            }
        }

        @Nested
        @DisplayName("and there is one event for the booking number")
        class WhenHasSingleActiveEvent {
            @BeforeEach
            void setup() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
                when(offenderRepository.findByNomsNumber(anyString())).thenReturn(Optional.of(Offender.builder().offenderId(99L).build()));
                when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                        .thenReturn(Optional.of(aCustodyEvent(StandardReference
                                .builder()
                                .codeDescription("In Custody")
                                .codeValue("X")
                                .build())));
            }

            @Test
            @DisplayName("then the custody element will be returned")
            void willReturnCustody() {
                final var custody = custodyService.getCustodyByBookNumber("G9542VP", "44463B");
                assertThat(custody.getStatus().getDescription()).isEqualTo("In Custody");
            }
        }
    }
    @Nested
    @DisplayName("when calling getCustodyByConvictionId")
    class WhenRetrievingByConvictionId {
        @Nested
        @DisplayName("and there no active events for the booking number")
        class WhenNoActiveEvents {
            @BeforeEach
            void setup() {
                when(offenderRepository.findByCrn(anyString())).thenReturn(Optional.of(Offender.builder().offenderId(99L).build()));
                when(convictionService.convictionFor(anyLong(), anyLong()))
                        .thenReturn(Optional.empty());
            }

            @Test
            @DisplayName("then a NotFoundException will be thrown")
            void willThrowNotFound() {
                assertThatThrownBy(() -> custodyService.getCustodyByConvictionId("X12345", 99L))
                        .isInstanceOf(NotFoundException.class);
            }
        }
        @Nested
        @DisplayName("and the offender is not found")
        class WhenNoOffenderFound {
            @BeforeEach
            void setup() {
                when(offenderRepository.findByCrn(anyString())).thenReturn(Optional.empty());
            }

            @Test
            @DisplayName("then a NotFoundException will be thrown")
            void willThrowNotFound() {
                assertThatThrownBy(() -> custodyService.getCustodyByConvictionId("X12345", 99L))
                        .isInstanceOf(NotFoundException.class);
            }
        }

        @Nested
        @DisplayName("and there is a custodial event for the conviction id")
        class WhenHasSingleActiveEvent {
            @BeforeEach
            void setup() {
                when(offenderRepository.findByCrn(anyString())).thenReturn(Optional.of(Offender
                        .builder()
                        .offenderId(99L)
                        .build()));
                when(convictionService.convictionFor(anyLong(), anyLong()))
                        .thenReturn(Optional.of(Conviction
                                .builder()
                                .custody(Custody.builder().status(KeyValue.builder().description("In Custody")
                                        .build()).build())
                                .build()));
            }

            @Test
            @DisplayName("then the custody element will be returned")
            void willReturnCustody() {
                final var custody = custodyService.getCustodyByConvictionId("X12345", 99L);
                assertThat(custody.getStatus().getDescription()).isEqualTo("In Custody");
            }
        }
        @Nested
        @DisplayName("and there is a non-custodial event for the conviction id")
        class WhenHasANonCustodialEvent {
            @BeforeEach
            void setup() {
                when(offenderRepository.findByCrn(anyString())).thenReturn(Optional.of(Offender
                        .builder()
                        .offenderId(99L)
                        .build()));
                when(convictionService.convictionFor(anyLong(), anyLong()))
                        .thenReturn(Optional.of(Conviction
                                .builder()
                                .custody(null)
                                .build()));
            }

            @Test
            @DisplayName("then a BadRequest will be thrown")
            void willThrowNotFound() {
                assertThatThrownBy(() -> custodyService.getCustodyByConvictionId("X12345", 99L))
                        .isInstanceOf(BadRequestException.class);
            }
        }
    }

    @Nested
    @DisplayName("when calling offenderRecalled")
    class WhenOffenderRecalled {
        @Nested
        @DisplayName("and there is more than one event for the offender")
        class WhenHasMultipleActiveEvents {
            @BeforeEach
            void setup() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
                when(convictionService.getActiveCustodialEvent(anyLong()))
                    .thenThrow(new ConvictionService.SingleActiveCustodyConvictionNotFoundException(99L, 2));
            }

            @Test
            @DisplayName("then a ConflictingRequestException will be thrown")
            void willThrowNotFound() {
                assertThatThrownBy(() -> custodyService.offenderRecalled("X12345", LocalDate.of(2020, 11, 22)))
                    .isInstanceOf(ConflictingRequestException.class);
            }
        }

        @Nested
        @DisplayName("and there are no active events for the offender")
        class WhenNoActiveEvents {
            @BeforeEach
            void setup() throws ConvictionService.SingleActiveCustodyConvictionNotFoundException {
                when(convictionService.getActiveCustodialEvent(anyLong()))
                    .thenThrow(new ConvictionService.SingleActiveCustodyConvictionNotFoundException(99L, 0));
            }

            @Test
            @DisplayName("then a ConflictingRequestException will be thrown")
            void willThrowNotFound() {
                assertThatThrownBy(() -> custodyService.offenderRecalled("X12345", LocalDate.of(2020, 11, 22)))
                    .isInstanceOf(ConflictingRequestException.class);
            }
        }

        @Nested
        @DisplayName("and the offender is not found")
        class WhenNoOffenderFound {
            @BeforeEach
            void setup() {
                when(offenderRepository.findByNomsNumber(anyString())).thenReturn(Optional.empty());
            }

            @Test
            @DisplayName("then a NotFoundException will be thrown")
            void willThrowNotFound() {
                assertThatThrownBy(() -> custodyService.offenderRecalled("X12345", LocalDate.of(2020, 11, 22)))
                    .isInstanceOf(NotFoundException.class);
            }
        }

        @Nested
        @DisplayName("and there is one active event for the offender")
        class WhenHasSingleActiveEvent {
            @BeforeEach
            void setup() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
                when(convictionService.getActiveCustodialEvent(anyLong())).thenReturn(aCustodyEvent());
            }

            @Test
            @DisplayName("then the custody event will be returned")
            void willReturnCustody() {
                final var custody = custodyService.offenderRecalled("G9542VP", LocalDate.of(2020, 11, 22));
                assertThat(custody.getStatus().getDescription()).isEqualTo("In Custody");
            }
        }
    }
}

package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import uk.gov.justice.digital.delius.controller.BadRequestException;
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
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDate;
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
        custodyService = new CustodyService(true, true, telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService);
        when(offenderRepository.findByNomsNumber(anyString())).thenReturn(Optional.of(Offender.builder().offenderId(99L).build()));
        when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                .thenReturn(Optional.of(EntityHelper.aCustodyEvent()));
    }

    @Nested
    class WhenUpdatingCustody {
        private final ArgumentMatcher<Map<String, String>> standardTelemetryAttributes =
                attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
                        Optional.ofNullable(attributes.get("bookingNumber")).filter(value -> value.equals("44463B")).isPresent() &&
                        Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("MDI")).isPresent();

        @BeforeEach
        public void setup() {
            when(referenceDataService.getPrisonLocationChangeCustodyEvent()).thenReturn(StandardReference.builder().codeValue("CPL").codeDescription("Change prison location").build());
            when(referenceDataService.getCustodyStatusChangeCustodyEvent()).thenReturn(StandardReference.builder().codeValue("TSC").codeDescription("Custody status change").build());
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));
        }


        @Test
        public void willCreateTelemetryEventWhenOffenderNotFound() {
            when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

            verify(telemetryClient).trackEvent(eq("P2PTransferOffenderNotFound"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willThrowExceptionWhenOffenderNotFound() {
            when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);

        }

        @Test
        public void willCreateTelemetryEventWhenConvictionNotFound() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "44463B")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

            verify(telemetryClient).trackEvent(eq("P2PTransferBookingNumberNotFound"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willThrowExceptionWhenBookingNumberNotFound() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "44463B")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);
        }


        @Test
        public void willCreateTelemetryEventWhenDuplicateConvictionsFound() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "44463B"))
                    .thenThrow(new ConvictionService.DuplicateActiveCustodialConvictionsException(2));

            assertThatThrownBy(() ->
                    custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

            verify(telemetryClient).trackEvent(eq("P2PTransferBookingNumberHasDuplicates"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willThrowExceptionWhenDuplicateConvictionsFound() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(99L, "44463B"))
                    .thenThrow(new ConvictionService.DuplicateActiveCustodialConvictionsException(2));

            assertThatThrownBy(() ->
                    custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        public void willCreateTelemetryEventWhenPrisonNotFound() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

            verify(telemetryClient).trackEvent(eq("P2PTransferPrisonNotFound"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willThrowExceptionWhenPrisonNotFound() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        public void willCreateTelemetryEventAndNothingElseWhenPrisonAlreadySet() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            final var newInstitution = aPrisonInstitution();
            final var currentInstitution = aPrisonInstitution();
            final var event = EntityHelper.aCustodyEvent();
            event.getDisposal().getCustody().setInstitution(currentInstitution);
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                    .thenReturn(Optional.of(event));
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(newInstitution));


            final var custody = custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdateIgnored"), argThat(standardTelemetryAttributes), isNull());

            verify(spgNotificationService, never()).notifyUpdateOfCustody(any(), any());
            verify(contactService, never()).addContactForPrisonLocationChange(any(), any());
            verify(offenderManagerService, never()).autoAllocatePrisonOffenderManagerAtInstitution(any(), any());

            assertThat(custody).isNotNull();
        }

        @Test
        public void willCreateTelemetryEventWhenPrisonLocationChanges() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution()));

            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdated"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willCreateCustodyHistoryChangeLocationEvent() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));
            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(custodyHistoryRepository).save(custodyHistoryArgumentCaptor.capture());

            final var custodyHistoryEvent = custodyHistoryArgumentCaptor.getValue();

            assertThat(custodyHistoryEvent.getCustodyEventType().getCodeValue()).isEqualTo("CPL");
            assertThat(custodyHistoryEvent.getDetail()).isEqualTo("HMP Highland");
            assertThat(custodyHistoryEvent.getWhen()).isEqualTo(LocalDate.now());
        }

        @Test
        public void willNotifySPGOfCustodyChange() {
            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(spgNotificationService).notifyUpdateOfCustody(any(), any());
        }

        @Test
        public void willCreateContactAboutPrisonLocationChange() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            final var offender = anOffender();
            final var event = aCustodyEvent();
            when(offenderRepository.findByNomsNumber(anyString())).thenReturn(Optional.of(offender));
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                    .thenReturn(Optional.of(event));

            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(contactService).addContactForPrisonLocationChange(offender, event);
        }

        @Test
        public void willCreateNewPrisonOffenderManagerWhenExistingPOMAtDifferentPrison() {
            final var offender = Offender.builder().offenderId(99L).build();
            final var institution = anInstitution();

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(institution));
            when(offenderManagerService.isPrisonOffenderManagerAtInstitution(any(), any())).thenReturn(false);

            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(offenderManagerService).autoAllocatePrisonOffenderManagerAtInstitution(offender, institution);
        }

        @Test
        public void willNotCreateNewPrisonOffenderManagerWhenExistingPOMAtSamePrison() {
            final var institution = anInstitution();

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(institution));
            when(offenderManagerService.isPrisonOffenderManagerAtInstitution(any(), any())).thenReturn(true);

            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(offenderManagerService, never()).autoAllocatePrisonOffenderManagerAtInstitution(any(), any());
        }

        @Test
        public void willCreateCustodyHistoryChangeCustodyStatusWhenCurrentlyOnlySentenced() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                    .thenReturn(Optional.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("A").codeDescription("Sentenced in custody").build())));

            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

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
        public void willGetInCustodyStatusWhenCurrentlyOnlySentenced() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                    .thenReturn(Optional.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("A").codeDescription("Sentenced in custody").build())));

            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(referenceDataService).getInCustodyCustodyStatus();
        }

        @Test
        public void willNotifySPGOfCustodyLocationChangeWhenCurrentlyOnlySentenced() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                    .thenReturn(Optional.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("A").codeDescription("Sentenced in custody").build())));

            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(spgNotificationService).notifyUpdateOfCustodyLocationChange(any(), any());
        }

        @Test
        public void willNotifySPGOfCustodyLocationChangeWhenCurrentlyInCustody() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                    .thenReturn(Optional.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("D").codeDescription("In Custody").build())));

            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            verify(spgNotificationService).notifyUpdateOfCustodyLocationChange(any(), any());
        }

        @Test
        public void willCreateTelemetryEventWhenPrisonLocationChangesButStatusNotCurrentlyInPrison() throws ConvictionService.DuplicateActiveCustodialConvictionsException {
            when(convictionService.getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                    .thenReturn(Optional.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("B").codeDescription("Released on Licence").build())));

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

            assertThatThrownBy(() ->
                    custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                    .isInstanceOf(NotFoundException.class);

            verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdateIgnored"), argThat(standardTelemetryAttributes), isNull());
        }

        @Test
        public void willUpdatePrisonInstitutionWillBeUpdatedWhenFeatureSwitchedOn() {
            custodyService = new CustodyService(true, true, telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService);

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

            final var updatedCustody = custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            assertThat(updatedCustody.getInstitution().getDescription()).isEqualTo("HMP Highland");
        }

        @Test
        public void willNotUpdatePrisonInstitutionWillBeUpdatedWhenFeatureSwitchedOff() {
            custodyService = new CustodyService(false, true, telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService);

            when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(anInstitution().toBuilder().description("HMP Highland").build()));

            final var updatedCustody = custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

            assertThat(updatedCustody.getInstitution().getDescription()).isNotEqualTo("HMP Highland");
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
        class WhenOffenderNotFound {
            @BeforeEach
            public void setup() {
                when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.empty());
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
                when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.of(offender));
            }

            @Test
            void bookingNumberWillBeUpdated() {
                var custody = custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

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
                    custodyService = new CustodyService(true, false, telemetryClient, offenderRepository, convictionService, institutionRepository, custodyHistoryRepository, referenceDataService, spgNotificationService, offenderManagerService, contactService, offenderPrisonerService);
                }

                @Test
                void nothingWillBeUpdated() {
                    var custody = custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

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
                    var custody = custodyService.updateCustodyBookingNumber("G9542VP", updateCustodyBookingNumber);

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
}
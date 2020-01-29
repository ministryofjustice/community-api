package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;
import uk.gov.justice.digital.delius.jpa.standard.entity.CustodyHistory;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.CustodyHistoryRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.*;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CustodyServiceTest {
    private CustodyService custodyService;

    private TelemetryClient telemetryClient = mock(TelemetryClient.class);
    private OffenderRepository offenderRepository = mock(OffenderRepository.class);
    private ConvictionService convictionService = mock(ConvictionService.class);
    private InstitutionRepository institutionRepository = mock(InstitutionRepository.class);
    private CustodyHistoryRepository custodyHistoryRepository = mock(CustodyHistoryRepository.class);
    private LookupSupplier lookupSupplier = mock(LookupSupplier.class);
    private ReferenceDataService referenceDataService = mock(ReferenceDataService.class);
    private ArgumentMatcher<Map<String, String>> standardTelemetryAttributes =
            attributes -> Optional.ofNullable(attributes.get("offenderNo")).filter(value -> value.equals("G9542VP")).isPresent() &&
            Optional.ofNullable(attributes.get("bookingNumber")).filter(value -> value.equals("44463B")).isPresent() &&
            Optional.ofNullable(attributes.get("toAgency")).filter(value -> value.equals("MDI")).isPresent();
    private final ConvictionTransformer convictionTransformer = new ConvictionTransformer(
            new MainOffenceTransformer(lookupSupplier),
            new AdditionalOffenceTransformer(lookupSupplier),
            new CourtAppearanceTransformer(new CourtReportTransformer(new CourtTransformer()), new CourtTransformer(), lookupSupplier),
            lookupSupplier,
            new InstitutionTransformer());
    private ArgumentCaptor<CustodyHistory> custodyHistoryArgumentCaptor = ArgumentCaptor.forClass(CustodyHistory.class);

    @Before
    public void setup() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        custodyService = new CustodyService(true, telemetryClient, offenderRepository, convictionService, institutionRepository, convictionTransformer, custodyHistoryRepository, referenceDataService);
        when(offenderRepository.findByNomsNumber(anyString())).thenReturn(Optional.of(Offender.builder().offenderId(99L).build()));
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                .thenReturn(Optional.of(EntityHelper.aCustodyEvent()));
        when(referenceDataService.getPrisonLocationChangeCustodyEvent()).thenReturn(StandardReference.builder().codeValue("CPL").codeDescription("Change prison location").build());
        when(referenceDataService.getCustodyStatusChangeCustodyEvent()).thenReturn(StandardReference.builder().codeValue("TSC").codeDescription("Custody status change").build());
        when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(EntityHelper.anInstitution().toBuilder().description("HMP Highland").build()));
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
    public void willCreateTelemetryEventWhenConvictionNotFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

        verify(telemetryClient).trackEvent(eq("P2PTransferBookingNumberNotFound"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void willThrowExceptionWhenBookingNumberNotFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                .isInstanceOf(NotFoundException.class);
    }


    @Test
    public void willCreateTelemetryEventWhenDuplicateConvictionsFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B"))
                .thenThrow(new ConvictionService.DuplicateConvictionsForBookingNumberException(2));

        assertThatThrownBy(() ->
                custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()));

        verify(telemetryClient).trackEvent(eq("P2PTransferBookingNumberHasDuplicates"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void willThrowExceptionWhenDuplicateConvictionsFound() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(99L, "44463B"))
                .thenThrow(new ConvictionService.DuplicateConvictionsForBookingNumberException(2));

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
    public void willCreateTelemetryEventWhenPrisonLocationChanges() {
        when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(EntityHelper.anInstitution()));

        custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

        verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdated"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void willCreateCustodyHistoryChangeLocationEvent() {
        when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(EntityHelper.anInstitution().toBuilder().description("HMP Highland").build()));
        custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

        verify(custodyHistoryRepository).save(custodyHistoryArgumentCaptor.capture());

        final var custodyHistoryEvent = custodyHistoryArgumentCaptor.getValue();

        assertThat(custodyHistoryEvent.getCustodyEventType().getCodeValue()).isEqualTo("CPL");
        assertThat(custodyHistoryEvent.getDetail()).isEqualTo("HMP Highland");
        assertThat(custodyHistoryEvent.getWhen()).isEqualTo(LocalDate.now());
    }

    @Test
    public void willCreateCustodyHistoryChangeCustodyStatusWhenCurrentlyOnlySentenced() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
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
    public void willGetInCustodyStatusWhenCurrentlyOnlySentenced() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                .thenReturn(Optional.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("A").codeDescription("Sentenced in custody").build())));

        custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

        verify(referenceDataService).getInCustodyCustodyStatus();
    }

    @Test
    public void willCreateTelemetryEventWhenPrisonLocationChangesButStatusNotCurrentlyInPrison() throws ConvictionService.DuplicateConvictionsForBookingNumberException {
        when(convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(anyLong(), anyString()))
                .thenReturn(Optional.of(EntityHelper.aCustodyEvent(StandardReference.builder().codeValue("B").codeDescription("Released on Licence").build())));

        when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(EntityHelper.anInstitution().toBuilder().description("HMP Highland").build()));

        assertThatThrownBy(() ->
                custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build()))
                .isInstanceOf(NotFoundException.class);

        verify(telemetryClient).trackEvent(eq("P2PTransferPrisonUpdateIgnored"), argThat(standardTelemetryAttributes), isNull());
    }

    @Test
    public void willUpdatePrisonInstitutionWillBeUpdatedWhenFeatureSwitchedOn() {
        custodyService = new CustodyService(true, telemetryClient, offenderRepository, convictionService, institutionRepository, convictionTransformer, custodyHistoryRepository, referenceDataService);

        when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(EntityHelper.anInstitution().toBuilder().description("HMP Highland").build()));

        final var updatedCustody = custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

        assertThat(updatedCustody.getInstitution().getDescription()).isEqualTo("HMP Highland");
    }

    @Test
    public void willNotUpdatePrisonInstitutionWillBeUpdatedWhenFeatureSwitchedOff() {
        custodyService = new CustodyService(false, telemetryClient, offenderRepository, convictionService, institutionRepository, convictionTransformer, custodyHistoryRepository, referenceDataService);

        when(institutionRepository.findByNomisCdeCode("MDI")).thenReturn(Optional.of(EntityHelper.anInstitution().toBuilder().description("HMP Highland").build()));

        final var updatedCustody = custodyService.updateCustody("G9542VP", "44463B", UpdateCustody.builder().nomsPrisonInstitutionCode("MDI").build());

        assertThat(updatedCustody.getInstitution().getDescription()).isNotEqualTo("HMP Highland");
    }

}